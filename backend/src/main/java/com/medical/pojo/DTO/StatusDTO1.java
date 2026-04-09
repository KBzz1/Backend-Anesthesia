package com.medical.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO1 {

    private Long totalCount;
    private List<PatientInfo> patientList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PatientInfo {
        private Long surgeryId;
        private String patientName;
    }
}
