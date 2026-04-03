package com.medical.service.impl;

import com.medical.mapper.PatientMapper;
import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.DTO.StatusDTO1;
import com.medical.pojo.DTO.StatusDTO2;
import com.medical.pojo.PatientStatus;
import com.medical.service.PatientStatusService;
import com.medical.service.QueueService;
import com.medical.utils.enums.PatientRegionEnum;
import com.medical.utils.constants.RedisConstants;
import com.medical.utils.constants.StatusConstants;
import com.medical.utils.constants.SuperPatientConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class   PatientStatusServiceImpl implements PatientStatusService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    PatientMapper patientMapper;
    @Autowired
    SurgeryMapper surgeryMapper;


    @Autowired
    @Lazy   // 避免循环依赖
    QueueService queueService;

    // 更新患者状态
    // 包含推送逻辑（注意新旧状态要一起推送）
    @Transactional
    public void updatePatientStatus(String surgeryId, Integer newStatusCode, boolean updateTimestamp) {
        // 参数校验
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            throw new IllegalArgumentException("手术ID不能为空");
        }
        Long surgeryIdLong = Long.valueOf(surgeryId);
        if (!surgeryMapper.existsBySurgeryId(surgeryIdLong)) {
            throw new IllegalArgumentException("手术ID不存在");
        }
        if (newStatusCode == null || newStatusCode < StatusConstants.EVALUATED || newStatusCode > StatusConstants.IN_RECOVERY) {
            throw new IllegalArgumentException("状态码必须在1-8之间");
        }
        ensureSuperPatientInitialized(surgeryId);

        if (newStatusCode == StatusConstants.IN_PREPARATION_AREA) {
            // 过号清除
            queueService.clearMissedID(surgeryIdLong);
        }

        // Redis Key定义
        String infoKey = RedisConstants.PATIENT_INFO_KEY + surgeryId;  // Hash结构：存储患者详细信息
        String newStatusKey = RedisConstants.PATIENT_STATE_KEY + newStatusCode;  // ZSet结构：存储该状态下的所有患者

        // 从Hash中获取旧状态码和旧时间戳
        String oldStatusStr = (String) stringRedisTemplate.opsForHash().get(infoKey, "statusCode");
        String oldTimestampStr = (String) stringRedisTemplate.opsForHash().get(infoKey, "updateTime");

        long timestamp;
        if (updateTimestamp) {
            // 使用新时间戳
            timestamp = System.currentTimeMillis();
        } else {
            // 延用旧时间戳，如果没有则使用当前时间
            if (oldTimestampStr != null && !oldTimestampStr.isEmpty()) {
                try {
                    timestamp = Long.parseLong(oldTimestampStr);
                    log.debug("延用旧时间戳: surgeryId={}, timestamp={}", surgeryId, timestamp);
                } catch (NumberFormatException e) {
                    log.warn("旧时间戳格式错误，使用当前时间: surgeryId={}, oldTimestamp={}", surgeryId, oldTimestampStr, e);
                    timestamp = System.currentTimeMillis();
                }
            } else {
                log.warn("未找到旧时间戳，使用当前时间: surgeryId={}", surgeryId);
                timestamp = System.currentTimeMillis();
            }
        }
        // 如果有旧状态，从旧状态的ZSet中移除
        Integer oldStatusCode = null;
        if (oldStatusStr != null && !oldStatusStr.isEmpty()) {
            try {
                oldStatusCode = Integer.valueOf(oldStatusStr);
                String oldStatusKey = RedisConstants.PATIENT_STATE_KEY + oldStatusCode;
                stringRedisTemplate.opsForZSet().remove(oldStatusKey, surgeryId);
                log.debug("从旧状态ZSet移除: surgeryId={}, oldStatus={}", surgeryId, oldStatusCode);
                // 旧状态推送
                pushPatientsByStatus(oldStatusCode);
            } catch (NumberFormatException e) {
                log.error("旧状态码格式错误: surgeryId={}, oldStatusStr={}", surgeryId, oldStatusStr, e);
            }
        }
        boolean allowBackwardTransition = SuperPatientConstants.isSuperPatient(surgeryIdLong)
                || Objects.equals(oldStatusCode, StatusConstants.CHECKED_IN);
        if (oldStatusCode != null && !allowBackwardTransition && newStatusCode < oldStatusCode) {
            throw new IllegalArgumentException("状态回退操作！");
        }
        // 更新患者详细信息到Hash结构
        Map<String, String> patientInfo = new HashMap<>();
        patientInfo.put("statusCode", String.valueOf(newStatusCode));
        patientInfo.put("updateTime", String.valueOf(timestamp));
        stringRedisTemplate.opsForHash().putAll(infoKey, patientInfo);
        log.debug("更新Hash信息: key={}, info={}", infoKey, patientInfo);
        // 添加到新状态的ZSet中（score为时间戳，用于排序）
        stringRedisTemplate.opsForZSet().add(newStatusKey, surgeryId, timestamp);
        queueService.syncAppointmentState(surgeryIdLong, newStatusCode);
        // 新状态推送
        pushPatientsByStatus(newStatusCode);
        pushAllRegionStatistics();

        // 如果状态变更为手术中，恢复中则需要将时间节点保存至数据库（且杜绝重复变更）
        if (!SuperPatientConstants.isSuperPatient(surgeryIdLong)
                && newStatusCode >= StatusConstants.IN_SURGERY
                && !Objects.equals(oldStatusCode, newStatusCode)) {
            LocalDateTime currentTime = LocalDateTime.now(); // 获取当前时间
            switch (newStatusCode) {
                case StatusConstants.IN_SURGERY:
                    surgeryMapper.updateSurgeryStartTime(surgeryIdLong, currentTime);
                    break;
                case StatusConstants.IN_RECOVERY:
                    surgeryMapper.updateSurgeryEndTime(surgeryIdLong, currentTime);
                    break;
            }
            log.info("时间节点保存成功: surgeryId={}, Status={}", surgeryId, newStatusCode);
        }
        log.info("患者状态更新成功: surgeryId={}, oldStatus={}, newStatus={}", surgeryId, oldStatusStr, newStatusCode);
    }

    // 根据手术ID查询患者状态
    public PatientStatus getPatientStatus(String surgeryId) {
        ensureSuperPatientInitialized(surgeryId);
        String infoKey = RedisConstants.PATIENT_INFO_KEY + surgeryId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(infoKey);
        if (entries.isEmpty()) {
            return null;
        }
        PatientStatus patientStatus = new PatientStatus();
        // 从String转换为Integer
        Object statusCodeObj = entries.get("statusCode");
        if (statusCodeObj != null) {
            patientStatus.setStatusCode(Integer.parseInt(statusCodeObj.toString()));
        }
        // 从String转换为Long（时间戳）
        Object updateTimeObj = entries.get("updateTime");
        if (updateTimeObj != null) {
            try {
                patientStatus.setUpdateTime(Long.parseLong(updateTimeObj.toString()));
            } catch (NumberFormatException e) {
                log.warn("时间戳格式转换失败: surgeryId={}, updateTime={}", surgeryId, updateTimeObj);
                // 可以选择设置为当前时间戳或null
                patientStatus.setUpdateTime(null);
            }
        }

        return patientStatus;
    }

    // 推送所有区域的患者数量统计
    public void pushAllRegionStatistics() {
        try {
            List<StatusDTO2> regionStatistics = getAllRegionStatistics();
            messagingTemplate.convertAndSend("/data/patients/status/statistics", regionStatistics);
            log.info("推送所有区域患者统计成功, 区域数: {}", regionStatistics.size());
        } catch (Exception e) {
            log.error("推送所有区域患者统计失败", e);
        }
    }

    // 区域整合
    public List<StatusDTO2> getAllRegionStatistics() {
        ensureSuperPatientInitialized(String.valueOf(SuperPatientConstants.SUPER_SURGERY_ID));
        List<StatusDTO2> result = new ArrayList<>();
        for (PatientRegionEnum region : PatientRegionEnum.values()) {
            StatusDTO2 dto = getRegionStatistics(region);
            result.add(dto);
        }
        return result;
    }
    // 获取单个区域的患者统计信息
    private StatusDTO2 getRegionStatistics(PatientRegionEnum region) {
        // 存储该区域所有患者的手术ID（去重）
        Set<String> allSurgeryIds = new LinkedHashSet<>();
        // 遍历该区域包含的所有状态码
        for (Integer statusCode : region.getStatusCodes()) {
            String statusKey = RedisConstants.PATIENT_STATE_KEY + statusCode;
            Set<String> surgeryIds = stringRedisTemplate.opsForZSet().range(statusKey, 0, -1);
            if (surgeryIds != null) {
                allSurgeryIds.addAll(surgeryIds);
            }
        }
        addSuperPatientWaitingReservation(region, allSurgeryIds);
        // 构建患者信息列表
        List<StatusDTO2.PatientInfo> patientList = new ArrayList<>();
        if (!allSurgeryIds.isEmpty()) {
            // 转换为 Long 类型
            List<Long> ids = allSurgeryIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            // 批量查询患者信息
            List<PatientDTO> patients = patientMapper.findByIds(ids);
            // 使用 Map 确保正确匹配
            Map<Long, String> patientMap = new HashMap<>();
            for (int i = 0; i < patients.size(); i++) {
                patientMap.put(ids.get(i), patients.get(i).getName());
            }
            // 构建结果列表
            for (Long id : ids) {
                String name = patientMap.getOrDefault(id, "未知患者");
                patientList.add(new StatusDTO2.PatientInfo(id, name));
            }
        }
        // 封装返回结果
        return new StatusDTO2(
            region.getRegionName(),
            (long) allSurgeryIds.size(),
            patientList
        );
    }

    // 推送某状态码下的所有患者信息
    public void pushPatientsByStatus(Integer statusCode) {
        try {
            StatusDTO1 dto = getPatientInfoByStatus(statusCode);
            messagingTemplate.convertAndSend("/data/patients/status/" + statusCode, dto);
            log.info("推送状态码患者列表成功, statusCode: {}", statusCode);
        } catch (Exception e) {
            log.error("推送状态码患者列表失败, statusCode: {}", statusCode, e);
        }
    }

    // 根据状态码统计患者信息
    public StatusDTO1 getPatientInfoByStatus(Integer statusCode) {
        ensureSuperPatientInitialized(String.valueOf(SuperPatientConstants.SUPER_SURGERY_ID));
        String statusKey = RedisConstants.PATIENT_STATE_KEY + statusCode;
        // 1. 获取患者数量
        Long count = stringRedisTemplate.opsForZSet().zCard(statusKey);
        // 2. 获取所有患者手术ID
        Set<String> surgeryIds = stringRedisTemplate.opsForZSet().range(statusKey, 0, -1);
        // 3. 构建患者信息列表
        List<StatusDTO1.PatientInfo> patientList = new ArrayList<>();
        if (surgeryIds != null && !surgeryIds.isEmpty()) {
            // 类型转换
            List<Long> ids = surgeryIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            // 从数据库批量查询患者姓名
            List<PatientDTO> patients = patientMapper.findByIds(ids);
            // 使用索引遍历，确保ID和患者对应
            for (int i = 0; i < patients.size(); i++) {
                patientList.add(new StatusDTO1.PatientInfo(
                        ids.get(i),
                        patients.get(i).getName()
                ));
            }
        }
        // 4. 封装返回结果
        return new StatusDTO1(count, patientList);
    }

    // 删除患者状态信息
    @Transactional
    public void deletePatientStatus(String surgeryId)   {
        try {
            ensureSuperPatientInitialized(surgeryId);
            // 1. 从Hash中获取当前状态码
            String infoKey = RedisConstants.PATIENT_INFO_KEY + surgeryId;
            Object statusCodeObj = stringRedisTemplate.opsForHash().get(infoKey, "statusCode");
            // 2. 如果存在状态，从状态ZSet中移除
            if (statusCodeObj != null) {
                try {
                    Integer statusCode = Integer.valueOf(statusCodeObj.toString());
                    String statusKey = RedisConstants.PATIENT_STATE_KEY + statusCode;
                    stringRedisTemplate.opsForZSet().remove(statusKey, surgeryId);
                    log.debug("从状态ZSet中移除患者: surgeryId={}, statusCode={}", surgeryId, statusCode);
                    // 状态码统计变更则推送
                    pushPatientsByStatus(statusCode);
                    pushAllRegionStatistics();
                } catch (NumberFormatException e) {
                    log.error("状态码格式错误: surgeryId={}, statusCode={}", surgeryId, statusCodeObj, e);
                }
            }
            // 3. 删除Hash详细信息
            stringRedisTemplate.delete(infoKey);
            if (SuperPatientConstants.isSuperPatient(surgeryId)) {
                queueService.clearAppointmentState(SuperPatientConstants.SUPER_SURGERY_ID);
                queueService.clearMissedID(SuperPatientConstants.SUPER_SURGERY_ID);
                initializeSuperPatientState();
                pushPatientsByStatus(StatusConstants.EVALUATED);
                pushAllRegionStatistics();
            }
            log.info("患者状态信息已删除: surgeryId={}", surgeryId);

        } catch (Exception e) {
            log.error("删除患者状态信息失败: surgeryId={}", surgeryId, e);
            throw new RuntimeException("删除患者状态信息失败", e);
        }
    }

    private void ensureSuperPatientInitialized(String surgeryId) {
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            return;
        }
        if (!surgeryMapper.existsBySurgeryId(SuperPatientConstants.SUPER_SURGERY_ID)) {
            throw new IllegalStateException("超级病人种子数据不存在，请先执行 sql/super_patient_seed.sql");
        }
        String infoKey = RedisConstants.PATIENT_INFO_KEY + surgeryId;
        Boolean exists = stringRedisTemplate.hasKey(infoKey);
        if (Boolean.TRUE.equals(exists)) {
            return;
        }
        initializeSuperPatientState();
    }

    private void initializeSuperPatientState() {
        String surgeryId = String.valueOf(SuperPatientConstants.SUPER_SURGERY_ID);
        long timestamp = System.currentTimeMillis();
        Map<String, String> patientInfo = new HashMap<>();
        patientInfo.put("statusCode", String.valueOf(StatusConstants.EVALUATED));
        patientInfo.put("updateTime", String.valueOf(timestamp));
        stringRedisTemplate.opsForHash().putAll(RedisConstants.PATIENT_INFO_KEY + surgeryId, patientInfo);
        stringRedisTemplate.opsForZSet().add(
                RedisConstants.PATIENT_STATE_KEY + StatusConstants.EVALUATED,
                surgeryId,
                timestamp
        );
    }

    private void addSuperPatientWaitingReservation(PatientRegionEnum region, Set<String> allSurgeryIds) {
        if (region != PatientRegionEnum.WAITING) {
            return;
        }
        PatientStatus status = getPatientStatus(String.valueOf(SuperPatientConstants.SUPER_SURGERY_ID));
        if (status != null && Objects.equals(status.getStatusCode(), StatusConstants.APPOINTED)) {
            allSurgeryIds.add(String.valueOf(SuperPatientConstants.SUPER_SURGERY_ID));
        }
    }

}
