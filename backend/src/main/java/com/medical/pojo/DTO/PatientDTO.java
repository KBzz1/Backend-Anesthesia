package com.medical.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// 排队响应数据包
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO {
    // Durable patient identity id, not the per-encounter surgery id.
    private Integer patientId;
    private String name;
    private String gender;
    private Integer age;
    private String idCardNumber;
    private Boolean isSoldier;

    // Treatment 表字段
    private Boolean isEmergency;
}

