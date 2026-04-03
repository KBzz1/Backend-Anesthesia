package com.medical.service;

import com.medical.pojo.DTO.DeviceBindingDTO;
import com.medical.pojo.Data;
import org.springframework.stereotype.Service;
import java.util.Map;

// 定义设备患者绑定的相关服务
@Service
public interface DeviceBindingService {

    // 绑定
    void bindDevice(String macAddress, String surgeryId);

    // 解绑
    void unbindDevice(String macAddress);
    void unbindDeviceBySurgeryId(String surgeryId);

    // 通过设备MAC地址获取设备绑定信息
    DeviceBindingDTO getPatient(String macAddress);
    // 通过患者手术ID查询绑定的设备MAC地址
    String getDevice(String surgeryId);

    // 获取当前所有绑定设备及其患者手术ID的配套信息
    Map<String, String> getBindingStatistics();











}
