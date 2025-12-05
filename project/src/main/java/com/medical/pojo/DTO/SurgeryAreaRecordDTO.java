package com.medical.pojo.DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class SurgeryAreaRecordDTO {
    private List<DrugRecordItem> drugRecord;
    private List<SurgeryRecordItem> surgeryRecord;

    @Data
    public static class DrugRecordItem {
        private String drugName;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "GMT+8")
        private Timestamp pushTime;
        private BigDecimal dosage;
        private String unit;
    }

    @Data
    public static class SurgeryRecordItem {
        private String eventName;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "GMT+8")
        private Timestamp eventTime;
    }
}
