package com.medical.pojo.request;

import lombok.Data;

@Data
public class BindDeviceRequest {
    private String surgeryId;
    private String macAddress;
}
