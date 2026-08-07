package com.medical.service.impl;

import com.medical.mapper.PatientMapper;
import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.DTO.StatusDTO1;
import com.medical.pojo.DTO.StatusDTO2;
import com.medical.pojo.PatientStatus;
import com.medical.service.PatientStatusService;
import com.medical.service.QueueService;
import com.medical.utils.constants.PatientFlowRedisKeys;
import com.medical.utils.constants.StatusConstants;
import com.medical.utils.constants.SuperPatientConstants;
import com.medical.utils.enums.PatientRegionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PatientStatusServiceImpl implements PatientStatusService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final PatientMapper patientMapper;
    private final SurgeryMapper surgeryMapper;
    private final QueueService queueService;

    public PatientStatusServiceImpl(
            StringRedisTemplate stringRedisTemplate,
            SimpMessagingTemplate messagingTemplate,
            PatientMapper patientMapper,
            SurgeryMapper surgeryMapper,
            @Lazy QueueService queueService
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.patientMapper = patientMapper;
        this.surgeryMapper = surgeryMapper;
        this.queueService = queueService;
    }

    @Override
    @Transactional
    public void updatePatientStatus(String surgeryId, Integer newStatusCode, boolean updateTimestamp) {
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            throw new IllegalArgumentException("手术ID不能为空");
        }
        Long surgeryIdLong = Long.valueOf(surgeryId);
        if (!surgeryMapper.existsBySurgeryId(surgeryIdLong)) {
            throw new IllegalArgumentException("手术ID不存在");
        }
        if (newStatusCode == null || newStatusCode < StatusConstants.EVALUATED || newStatusCode > StatusConstants.MISSED) {
            throw new IllegalArgumentException("状态码必须在" + StatusConstants.EVALUATED + "-" + StatusConstants.MISSED + "之间");
        }
        ensureSuperPatientInitialized(surgeryId);

        String infoKey = PatientFlowRedisKeys.PATIENT_INFO_KEY + surgeryId;
        String newStatusKey = PatientFlowRedisKeys.PATIENT_STATE_KEY + newStatusCode;
        String oldStatusStr = (String) stringRedisTemplate.opsForHash().get(infoKey, "statusCode");
        String oldTimestampStr = (String) stringRedisTemplate.opsForHash().get(infoKey, "updateTime");
        long timestamp = resolveTimestamp(updateTimestamp, oldTimestampStr, surgeryId);

        Integer oldStatusCode = null;
        if (oldStatusStr != null && !oldStatusStr.isEmpty()) {
            try {
                oldStatusCode = Integer.valueOf(oldStatusStr);
                String oldStatusKey = PatientFlowRedisKeys.PATIENT_STATE_KEY + oldStatusCode;
                stringRedisTemplate.opsForZSet().remove(oldStatusKey, surgeryId);
                pushPatientsByStatus(oldStatusCode);
            } catch (NumberFormatException e) {
                log.error("旧状态码格式错误: surgeryId={}, oldStatusStr={}", surgeryId, oldStatusStr, e);
            }
        }

        boolean allowBackwardTransition = SuperPatientConstants.isSuperPatient(surgeryIdLong)
                || Objects.equals(oldStatusCode, StatusConstants.CHECKED_IN)
                || Objects.equals(oldStatusCode, StatusConstants.MISSED);
        if (oldStatusCode != null && !allowBackwardTransition && newStatusCode < oldStatusCode) {
            throw new IllegalArgumentException("状态回退操作！");
        }

        Map<String, String> patientInfo = new HashMap<>();
        patientInfo.put("statusCode", String.valueOf(newStatusCode));
        patientInfo.put("updateTime", String.valueOf(timestamp));
        stringRedisTemplate.opsForHash().putAll(infoKey, patientInfo);
        stringRedisTemplate.opsForZSet().add(newStatusKey, surgeryId, timestamp);

        queueService.syncAppointmentState(surgeryIdLong, newStatusCode);
        pushPatientsByStatus(newStatusCode);
        pushAllRegionStatistics();

        if (!SuperPatientConstants.isSuperPatient(surgeryIdLong)
                && newStatusCode >= StatusConstants.IN_SURGERY
                && !Objects.equals(oldStatusCode, newStatusCode)) {
            LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("GMT+8"));
            switch (newStatusCode) {
                case StatusConstants.IN_SURGERY -> surgeryMapper.updateSurgeryStartTime(surgeryIdLong, currentTime);
                case StatusConstants.IN_RECOVERY -> surgeryMapper.updateSurgeryEndTime(surgeryIdLong, currentTime);
                default -> {
                }
            }
        }
    }

    @Override
    public PatientStatus getPatientStatus(String surgeryId) {
        ensureSuperPatientInitialized(surgeryId);
        String infoKey = PatientFlowRedisKeys.PATIENT_INFO_KEY + surgeryId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(infoKey);
        if (entries.isEmpty()) {
            return null;
        }
        PatientStatus patientStatus = new PatientStatus();
        Object statusCodeObj = entries.get("statusCode");
        if (statusCodeObj != null) {
            patientStatus.setStatusCode(Integer.parseInt(statusCodeObj.toString()));
        }
        Object updateTimeObj = entries.get("updateTime");
        if (updateTimeObj != null) {
            try {
                patientStatus.setUpdateTime(Long.parseLong(updateTimeObj.toString()));
            } catch (NumberFormatException e) {
                log.warn("时间戳格式转换失败: surgeryId={}, updateTime={}", surgeryId, updateTimeObj);
                patientStatus.setUpdateTime(null);
            }
        }
        return patientStatus;
    }

    @Override
    public void pushAllRegionStatistics() {
        try {
            List<StatusDTO2> regionStatistics = getAllRegionStatistics();
            messagingTemplate.convertAndSend("/data/patients/status/statistics", regionStatistics);
        } catch (Exception e) {
            log.error("推送所有区域患者统计失败", e);
        }
    }

    @Override
    public List<StatusDTO2> getAllRegionStatistics() {
        ensureSuperPatientInitialized(String.valueOf(SuperPatientConstants.SUPER_SURGERY_ID));
        List<StatusDTO2> result = new ArrayList<>();
        for (PatientRegionEnum region : PatientRegionEnum.values()) {
            result.add(getRegionStatistics(region));
        }
        return result;
    }

    @Override
    public void pushPatientsByStatus(Integer statusCode) {
        try {
            StatusDTO1 dto = getPatientInfoByStatus(statusCode);
            messagingTemplate.convertAndSend("/data/patients/status/" + statusCode, dto);
        } catch (Exception e) {
            log.error("推送状态码患者列表失败, statusCode: {}", statusCode, e);
        }
    }

    @Override
    public StatusDTO1 getPatientInfoByStatus(Integer statusCode) {
        ensureSuperPatientInitialized(String.valueOf(SuperPatientConstants.SUPER_SURGERY_ID));
        String statusKey = PatientFlowRedisKeys.PATIENT_STATE_KEY + statusCode;
        Long count = stringRedisTemplate.opsForZSet().zCard(statusKey);
        Set<String> surgeryIds = stringRedisTemplate.opsForZSet().range(statusKey, 0, -1);
        List<StatusDTO1.PatientInfo> patientList = new ArrayList<>();
        if (surgeryIds != null && !surgeryIds.isEmpty()) {
            List<Long> ids = surgeryIds.stream().map(Long::valueOf).collect(Collectors.toList());
            List<PatientDTO> patients = patientMapper.findByIds(ids);
            for (int i = 0; i < patients.size(); i++) {
                patientList.add(new StatusDTO1.PatientInfo(ids.get(i), patients.get(i).getName()));
            }
        }
        return new StatusDTO1(count, patientList);
    }

    @Override
    @Transactional
    public void deletePatientStatus(String surgeryId) {
        try {
            ensureSuperPatientInitialized(surgeryId);
            String infoKey = PatientFlowRedisKeys.PATIENT_INFO_KEY + surgeryId;
            Object statusCodeObj = stringRedisTemplate.opsForHash().get(infoKey, "statusCode");
            if (statusCodeObj != null) {
                try {
                    Integer statusCode = Integer.valueOf(statusCodeObj.toString());
                    String statusKey = PatientFlowRedisKeys.PATIENT_STATE_KEY + statusCode;
                    stringRedisTemplate.opsForZSet().remove(statusKey, surgeryId);
                    pushPatientsByStatus(statusCode);
                    pushAllRegionStatistics();
                } catch (NumberFormatException e) {
                    log.error("状态码格式错误: surgeryId={}, statusCode={}", surgeryId, statusCodeObj, e);
                }
            }
            stringRedisTemplate.delete(infoKey);
            if (SuperPatientConstants.isSuperPatient(surgeryId)) {
                queueService.clearAppointmentState(SuperPatientConstants.SUPER_SURGERY_ID);
                initializeSuperPatientState();
                pushPatientsByStatus(StatusConstants.EVALUATED);
                pushAllRegionStatistics();
            }
        } catch (Exception e) {
            log.error("删除患者状态信息失败: surgeryId={}", surgeryId, e);
            throw new RuntimeException("删除患者状态信息失败", e);
        }
    }

    private long resolveTimestamp(boolean updateTimestamp, String oldTimestampStr, String surgeryId) {
        if (updateTimestamp) {
            return System.currentTimeMillis();
        }
        if (oldTimestampStr != null && !oldTimestampStr.isEmpty()) {
            try {
                return Long.parseLong(oldTimestampStr);
            } catch (NumberFormatException e) {
                log.warn("旧时间戳格式错误，使用当前时间: surgeryId={}, oldTimestamp={}", surgeryId, oldTimestampStr, e);
            }
        }
        return System.currentTimeMillis();
    }

    private StatusDTO2 getRegionStatistics(PatientRegionEnum region) {
        Set<String> allSurgeryIds = new LinkedHashSet<>();
        for (Integer statusCode : region.getStatusCodes()) {
            String statusKey = PatientFlowRedisKeys.PATIENT_STATE_KEY + statusCode;
            Set<String> surgeryIds = stringRedisTemplate.opsForZSet().range(statusKey, 0, -1);
            if (surgeryIds != null) {
                allSurgeryIds.addAll(surgeryIds);
            }
        }
        addSuperPatientWaitingReservation(region, allSurgeryIds);
        List<StatusDTO2.PatientInfo> patientList = new ArrayList<>();
        if (!allSurgeryIds.isEmpty()) {
            List<Long> ids = allSurgeryIds.stream().map(Long::valueOf).collect(Collectors.toList());
            List<PatientDTO> patients = patientMapper.findByIds(ids);
            Map<Long, String> patientMap = new HashMap<>();
            for (int i = 0; i < patients.size(); i++) {
                patientMap.put(ids.get(i), patients.get(i).getName());
            }
            for (Long id : ids) {
                patientList.add(new StatusDTO2.PatientInfo(id, patientMap.getOrDefault(id, "未知患者")));
            }
        }
        return new StatusDTO2(region.getRegionName(), (long) allSurgeryIds.size(), patientList);
    }

    private void ensureSuperPatientInitialized(String surgeryId) {
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            return;
        }
        if (!surgeryMapper.existsBySurgeryId(SuperPatientConstants.SUPER_SURGERY_ID)) {
            throw new IllegalStateException("超级病人种子数据不存在，请先执行 sql/super_patient_seed.sql");
        }
        String infoKey = PatientFlowRedisKeys.PATIENT_INFO_KEY + surgeryId;
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
        stringRedisTemplate.opsForHash().putAll(PatientFlowRedisKeys.PATIENT_INFO_KEY + surgeryId, patientInfo);
        stringRedisTemplate.opsForZSet().add(
                PatientFlowRedisKeys.PATIENT_STATE_KEY + StatusConstants.EVALUATED,
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
