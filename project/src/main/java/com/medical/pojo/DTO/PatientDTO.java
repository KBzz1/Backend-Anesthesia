package com.medical.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
// 排队响应数据包
@Data
@AllArgsConstructor
public class PatientDTO {
    private Integer patientId;
    private String name;
    private String gender;
    private Integer age;
    private Boolean isSoldier;

    // Treatment 表字段
    private Boolean isEmergency;
}

