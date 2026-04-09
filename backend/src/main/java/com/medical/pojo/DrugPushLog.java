package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrugPushLog {
    private Integer id;
    private String drugName;
    private Timestamp pushTime;
    private BigDecimal dosage;
    private String unit;
    private Long treatmentInformationId;
}
