package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Surgery {
    // 还差一些字段如创建时间等
    private Long surgeryId;
    private Long patientId;
    // 术式及麻醉方式来自前端第二段 treatmentInformation
    private String surgeryMethod;
    private String otherSurgeryMethod;
    private String anesthesiaMethod;
    // 急诊标识
    private Boolean isEmergency;
}

