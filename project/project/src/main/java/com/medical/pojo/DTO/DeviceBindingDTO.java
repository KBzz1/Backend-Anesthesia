package com.medical.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceBindingDTO {
    /**
     * 患者手术ID
     */
    private String surgeryId;

    /**
     * 绑定时间
     */
    private Long bindTime;

//    /**
//     * 设备名称
//     */
//    private String deviceName;
//
//    /**
//     * 设备类型
//     */
//    private String deviceType;
}

