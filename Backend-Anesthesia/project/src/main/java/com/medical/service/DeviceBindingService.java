package com.medical.service;

import com.medical.pojo.BindingInfo;

public interface DeviceBindingService {
    BindingInfo getBindingInfo(String deviceId);
}
