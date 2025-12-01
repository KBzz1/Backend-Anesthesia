package com.medical.pojo.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
public class SurgeryAreaRecordDTO {
    private List<DrugRecordItem> drugRecord;
    private List<SurgeryRecordItem> surgeryRecord;

    @Data
    public static class DrugRecordItem {
        private String drugName;
        private Timestamp pushTime;
        private BigDecimal dosage;
        private String unit;
    }

    @Data
    public static class SurgeryRecordItem {
        private String eventName;
        private Timestamp eventTime;
    }
}
