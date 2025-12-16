package com.medical.pojo.DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DrugRecordItemDTO {
    private String drugName;

    private BigDecimal dosage;

    private String unit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp pushTime;
}
