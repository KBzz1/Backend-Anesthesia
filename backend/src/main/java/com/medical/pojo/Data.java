package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Data {
    // surgeryId for /data/sub/all endpoint (maps to treatment_information_id)
    private String surgeryId;
    private int resp;
    private int bo;
    private int hr;
    private float temp;
    private float ecg;
    private float boWave;
    private float respWave;
    // 设备端采集时间（客户端传递时间戳）
    private long timestamp = Instant.now().toEpochMilli();
    private float bp;
}

