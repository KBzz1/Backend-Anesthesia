package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    private Integer patientId;
    private String name;
    private String gender;
    private Integer age;
    private Boolean isSoldier;
    private String idCardNumber;
//    private Boolean isEmergency;
}

