package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurgeryStep {
    private Long surgeryStepId;
    private Long treatmentInformationId;
    private String stepName;
    private Timestamp stepTime;
}
