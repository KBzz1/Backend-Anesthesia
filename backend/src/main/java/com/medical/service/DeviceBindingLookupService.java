package com.medical.service;

import com.medical.pojo.BindingInfo;

public interface DeviceBindingLookupService {
    BindingInfo getBindingInfo(String deviceId);
}
