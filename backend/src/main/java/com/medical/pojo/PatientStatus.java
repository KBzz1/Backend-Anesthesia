package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientStatus {
    private Integer statusCode;
    private Long updateTime;
}
