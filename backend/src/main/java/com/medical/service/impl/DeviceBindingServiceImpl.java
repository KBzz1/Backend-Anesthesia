package com.medical.service.impl;

import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.DTO.DeviceBindingDTO;
import com.medical.service.DeviceBindingService;
import com.medical.utils.constants.DeviceBindingRedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class DeviceBindingServiceImpl implements DeviceBindingService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SurgeryMapper surgeryMapper;

    public DeviceBindingServiceImpl(StringRedisTemplate stringRedisTemplate, SurgeryMapper surgeryMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.surgeryMapper = surgeryMapper;
    }

    @Override
    public void bindDevice(String surgeryId, String macAddress) {
        if (macAddress == null || macAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("设备MAC地址不能为空");
        }
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            throw new IllegalArgumentException("患者手术ID不能为空");
        }

        try {
            long bindTime = System.currentTimeMillis();

            String deviceKey = DeviceBindingRedisKeys.DEVICE_BINDING_KEY + macAddress;
            Object existingSurgeryId = stringRedisTemplate.opsForHash().get(deviceKey, "surgeryId");
            if (existingSurgeryId != null && !existingSurgeryId.equals(surgeryId)) {
                String oldPatientDeviceKey = DeviceBindingRedisKeys.PATIENT_DEVICE_KEY + existingSurgeryId;
                stringRedisTemplate.delete(oldPatientDeviceKey);
            }

            String patientDeviceKey = DeviceBindingRedisKeys.PATIENT_DEVICE_KEY + surgeryId;
            String existingMacAddress = stringRedisTemplate.opsForValue().get(patientDeviceKey);
            if (existingMacAddress != null && !existingMacAddress.equals(macAddress)) {
                String oldDeviceKey = DeviceBindingRedisKeys.DEVICE_BINDING_KEY + existingMacAddress;
                stringRedisTemplate.delete(oldDeviceKey);
                stringRedisTemplate.opsForSet().remove(DeviceBindingRedisKeys.DEVICE_BOUND_SET, existingMacAddress);
            }

            Map<String, String> bindingInfo = new HashMap<>();
            bindingInfo.put("surgeryId", surgeryId);
            bindingInfo.put("bindTime", String.valueOf(bindTime));
            stringRedisTemplate.opsForHash().putAll(deviceKey, bindingInfo);
            stringRedisTemplate.opsForValue().set(patientDeviceKey, macAddress);
            stringRedisTemplate.opsForSet().add(DeviceBindingRedisKeys.DEVICE_BOUND_SET, macAddress);

            surgeryMapper.updateDeviceBindTime(
                    Long.valueOf(surgeryId),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(bindTime), ZoneId.systemDefault())
            );

            log.info("设备绑定成功: macAddress={}, surgeryId={}", macAddress, surgeryId);
        } catch (Exception e) {
            log.error("设备绑定失败: macAddress={}, surgeryId={}", macAddress, surgeryId, e);
            throw new RuntimeException("设备绑定失败", e);
        }
    }

    @Override
    public void unbindDevice(String macAddress) {
        if (macAddress == null || macAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("设备MAC地址不能为空");
        }

        try {
            String deviceKey = DeviceBindingRedisKeys.DEVICE_BINDING_KEY + macAddress;
            Object surgeryIdObj = stringRedisTemplate.opsForHash().get(deviceKey, "surgeryId");

            if (surgeryIdObj != null) {
                String surgeryId = surgeryIdObj.toString();
                String patientDeviceKey = DeviceBindingRedisKeys.PATIENT_DEVICE_KEY + surgeryId;
                stringRedisTemplate.delete(patientDeviceKey);
            }

            stringRedisTemplate.delete(deviceKey);
            stringRedisTemplate.opsForSet().remove(DeviceBindingRedisKeys.DEVICE_BOUND_SET, macAddress);
            log.info("设备解绑成功: macAddress={}", macAddress);
        } catch (Exception e) {
            log.error("设备解绑失败: macAddress={}", macAddress, e);
            throw new RuntimeException("设备解绑失败", e);
        }
    }

    @Override
    public void unbindDeviceBySurgeryId(String surgeryId) {
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            throw new IllegalArgumentException("患者手术ID不能为空");
        }

        try {
            String patientDeviceKey = DeviceBindingRedisKeys.PATIENT_DEVICE_KEY + surgeryId;
            String macAddress = stringRedisTemplate.opsForValue().get(patientDeviceKey);

            if (macAddress != null && !macAddress.isEmpty()) {
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

    @Override
    public DeviceBindingDTO getPatient(String macAddress) {
        if (macAddress == null || macAddress.trim().isEmpty()) {
            return null;
        }

        try {
            String deviceKey = DeviceBindingRedisKeys.DEVICE_BINDING_KEY + macAddress;
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

    @Override
    public String getDevice(String surgeryId) {
        if (surgeryId == null || surgeryId.trim().isEmpty()) {
            return null;
        }

        try {
            String patientDeviceKey = DeviceBindingRedisKeys.PATIENT_DEVICE_KEY + surgeryId;
            return stringRedisTemplate.opsForValue().get(patientDeviceKey);
        } catch (Exception e) {
            log.error("通过患者ID查询设备失败: surgeryId={}", surgeryId, e);
            return null;
        }
    }

    @Override
    public Map<String, String> getBindingStatistics() {
        Map<String, String> statistics = new HashMap<>();
        try {
            Set<String> boundDevices = stringRedisTemplate.opsForSet().members(DeviceBindingRedisKeys.DEVICE_BOUND_SET);
            if (boundDevices != null && !boundDevices.isEmpty()) {
                for (String macAddress : boundDevices) {
                    String deviceKey = DeviceBindingRedisKeys.DEVICE_BINDING_KEY + macAddress;
                    Object surgeryIdObj = stringRedisTemplate.opsForHash().get(deviceKey, "surgeryId");
                    if (surgeryIdObj != null) {
                        statistics.put(macAddress, surgeryIdObj.toString());
                    }
                }
            }
            return statistics;
        } catch (Exception e) {
            log.error("获取绑定统计失败", e);
            return statistics;
        }
    }
}
