package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Surgery {
    // surgeryId is the single-encounter business id for one treatment/surgery flow.
    private Long surgeryId;
    // patientId points back to the durable patient identity.
    private Long patientId;
    // payment state belongs to the encounter, not the durable patient profile.
    private Boolean isPaid;
    // 术式及麻醉方式来自前端第二段 treatmentInformation
    private String surgeryMethod;
    private String otherSurgeryMethod;
    private String anesthesiaMethod;
    // 急诊标识
    private Boolean isEmergency;
}

