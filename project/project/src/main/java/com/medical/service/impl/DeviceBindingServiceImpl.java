package com.medical.service.impl;

import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.DTO.DeviceBindingDTO;
import com.medical.service.DeviceBindingService;
import com.medical.utils.constants.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
public class DeviceBindingServiceImpl implements DeviceBindingService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SurgeryMapper surgeryMapper;

    /**
     * 绑定设备与患者（自动处理旧绑定关系）
     */
    @Override
    public void bindDevice(String surgeryId, String macAddress) {
        // 参数校验
        if (macAddress == null || macAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("设备MAC地址不能为空");
        }
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            throw new IllegalArgumentException("患者手术ID不能为空");
        }

        try {
            long bindTime = System.currentTimeMillis();

            // 1. 检查设备是否已绑定其他患者，如果是则先解绑
            String deviceKey = RedisConstants.DEVICE_BINDING_KEY + macAddress;
            Object existingSurgeryId = stringRedisTemplate.opsForHash().get(deviceKey, "surgeryId");

            if (existingSurgeryId != null && !existingSurgeryId.equals(surgeryId)) {
                log.info("设备{}已绑定患者{}，将自动解绑", macAddress, existingSurgeryId);
                // 删除旧患者的设备绑定
                String oldPatientDeviceKey = RedisConstants.PATIENT_DEVICE_KEY + existingSurgeryId;
                stringRedisTemplate.delete(oldPatientDeviceKey);
            }

            // 2. 检查患者是否已绑定其他设备，如果是则先解绑
            String patientDeviceKey = RedisConstants.PATIENT_DEVICE_KEY + surgeryId;
            String existingMacAddress = stringRedisTemplate.opsForValue().get(patientDeviceKey);

            if (existingMacAddress != null && !existingMacAddress.equals(macAddress)) {
                log.info("患者{}已绑定设备{}，将自动解绑", surgeryId, existingMacAddress);
                // 删除旧设备的绑定信息
                String oldDeviceKey = RedisConstants.DEVICE_BINDING_KEY + existingMacAddress;
                stringRedisTemplate.delete(oldDeviceKey);
                // 从已绑定集合中移除旧设备
                stringRedisTemplate.opsForSet().remove(RedisConstants.DEVICE_BOUND_SET, existingMacAddress);
            }

            // 3. 存储设备绑定信息（Hash）
            Map<String, String> bindingInfo = new HashMap<>();
            // 绑定信息：患者手术id + 绑定时间
            bindingInfo.put("surgeryId", surgeryId);
            bindingInfo.put("bindTime", String.valueOf(bindTime));
            stringRedisTemplate.opsForHash().putAll(deviceKey, bindingInfo);

            surgeryMapper.updateDeviceBindTime(
                    Long.valueOf(surgeryId),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(bindTime), ZoneId.systemDefault())
            );

            // 4. 存储患者绑定的设备（String）
            stringRedisTemplate.opsForValue().set(patientDeviceKey, macAddress);

            // 5. 添加到已绑定设备集合（Set）
            stringRedisTemplate.opsForSet().add(RedisConstants.DEVICE_BOUND_SET, macAddress);

            log.info("设备绑定成功: macAddress={}, surgeryId={}",
                    macAddress, surgeryId);

            // 绑定成功则

        } catch (Exception e) {
            log.error("设备绑定失败: macAddress={}, surgeryId={}", macAddress, surgeryId, e);
            throw new RuntimeException("设备绑定失败", e);
        }
    }

    /**
     * 通过设备MAC地址解绑
     */
    @Override
    public void unbindDevice(String macAddress) {
        if (macAddress == null || macAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("设备MAC地址不能为空");
        }

        try {
            // 1. 获取绑定信息
            String deviceKey = RedisConstants.DEVICE_BINDING_KEY + macAddress;
            Object surgeryIdObj = stringRedisTemplate.opsForHash().get(deviceKey, "surgeryId");

            if (surgeryIdObj != null) {
                String surgeryId = surgeryIdObj.toString();

                // 2. 删除患者绑定的设备信息
                String patientDeviceKey = RedisConstants.PATIENT_DEVICE_KEY + surgeryId;
                stringRedisTemplate.delete(patientDeviceKey);

                log.debug("删除患者设备绑定: surgeryId={}, macAddress={}", surgeryId, macAddress);
            }

            // 3. 删除设备绑定信息
            stringRedisTemplate.delete(deviceKey);

            // 4. 从已绑定设备集合中移除
            stringRedisTemplate.opsForSet().remove(RedisConstants.DEVICE_BOUND_SET, macAddress);

            log.info("设备解绑成功: macAddress={}", macAddress);

        } catch (Exception e) {
            log.error("设备解绑失败: macAddress={}", macAddress, e);
            throw new RuntimeException("设备解绑失败", e);
        }
    }

    /**
     * 通过患者手术ID解绑
     */
    @Override
    public void unbindDeviceBySurgeryId(String surgeryId) {
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            throw new IllegalArgumentException("患者手术ID不能为空");
        }

        try {
            // 1. 获取患者绑定的设备MAC地址
            String patientDeviceKey = RedisConstants.PATIENT_DEVICE_KEY + surgeryId;
            String macAddress = stringRedisTemplate.opsForValue().get(patientDeviceKey);

            if (macAddress != null && !macAddress.isEmpty()) {
                // 2. 调用设备解绑方法
                unbindDevice(macAddress);
                log.info("通过患者ID解绑设备成功: surgeryId={}, macAddress={}", surgeryId, macAddress);
            } else {
                log.warn("患者未绑定任何设备: surgeryId={}", surgeryId);
            }

        } catch (Exception e) {
            log.error("通过患者ID解绑设备失败: surgeryId={}", surgeryId, e);
            throw new RuntimeException("通过患者ID解绑设备失败", e);
        }
    }

    /**
     * 通过设备MAC地址获取绑定信息
     */
    @Override
    public DeviceBindingDTO getPatient(String macAddress) {
        if (macAddress == null || macAddress.trim().isEmpty()) {
            return null;
        }
        try {
            String deviceKey = RedisConstants.DEVICE_BINDING_KEY + macAddress;
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(deviceKey);

            if (entries.isEmpty()) {
                return null;
            }
            DeviceBindingDTO dto = new DeviceBindingDTO();
            dto.setSurgeryId((String) entries.get("surgeryId"));
            dto.setBindTime(Long.valueOf((String) entries.get("bindTime")));
            return dto;
        } catch (Exception e) {
            log.error("获取设备绑定信息失败: macAddress={}", macAddress, e);
            return null;
        }
    }

    /**
     * 通过患者手术ID查询绑定的设备MAC地址
     */
    @Override
    public String getDevice(String surgeryId) {
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            return null;
        }

        try {
            String patientDeviceKey = RedisConstants.PATIENT_DEVICE_KEY + surgeryId;
            return stringRedisTemplate.opsForValue().get(patientDeviceKey);

        } catch (Exception e) {
            log.error("通过患者ID查询设备失败: surgeryId={}", surgeryId, e);
            return null;
        }
    }

    /**
     * 获取所有绑定设备及其患者的映射关系
     */
    @Override
    public Map<String, String> getBindingStatistics() {
        Map<String, String> statistics = new HashMap<>();
        try {
            // 获取所有已绑定的设备MAC地址
            Set<String> boundDevices = stringRedisTemplate.opsForSet()
                    .members(RedisConstants.DEVICE_BOUND_SET);

            if (boundDevices != null && !boundDevices.isEmpty()) {
                for (String macAddress : boundDevices) {
                    String deviceKey = RedisConstants.DEVICE_BINDING_KEY + macAddress;
                    Object surgeryIdObj = stringRedisTemplate.opsForHash().get(deviceKey, "surgeryId");

                    if (surgeryIdObj != null) {
                        statistics.put(macAddress, surgeryIdObj.toString());
                    }
                }
            }
            log.info("获取绑定统计成功，共{}个设备已绑定", statistics.size());
            return statistics;
        } catch (Exception e) {
            log.error("获取绑定统计失败", e);
            return statistics;
        }
    }
}






