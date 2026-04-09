package com.medical.service;

import com.medical.pojo.DTO.DeviceBindingDTO;

import java.util.Map;

public interface DeviceBindingService {
    void bindDevice(String surgeryId, String macAddress);

    void unbindDevice(String macAddress);

    void unbindDeviceBySurgeryId(String surgeryId);

    DeviceBindingDTO getPatient(String macAddress);

    String getDevice(String surgeryId);

    Map<String, String> getBindingStatistics();
}
