package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Patient {
    private Integer patientId;
    private String name;
    private String gender;
    private Integer age;
    private Boolean isSoldier;
//    private Boolean isEmergency;
}

