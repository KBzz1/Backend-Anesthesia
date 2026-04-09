package com.medical.pojo.DTO;

import lombok.Data;

@Data
public class PatientSummaryDTO {
    private String name;
    // Per-encounter business id for the current treatment/surgery.
    private Long surgeryId;
}
