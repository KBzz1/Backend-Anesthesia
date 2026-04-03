package com.medical.pojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medical.pojo.PaaInformation;
import lombok.Data;

/**
 * 前端一次性提交患者、诊疗、麻醉评估三段数据时的载体。
 */
@Data
public class PaaAssessmentRequest {

    /**
     * 患者表（patient）对应的字段片段。
     */
    private PatientSection patient;

    /**
     * 诊疗信息表（treatment_information）的字段片段。
     */
    private TreatmentInformationSection treatmentInformation;

    /**
     * 麻醉评估表（paa_information）的完整入参，直接复用实体字段。
     */
    private PaaInformation paa;

    @Data
    public static class PatientSection {
        // 以下字段保持与 patient 表一一对应，便于后续扩展
        private String name;
        private String gender;
        private Integer age;
        private Boolean isSoldier;
        @JsonProperty("id_card_number")
        private String idCardNumber;
    }

    @Data
    public static class TreatmentInformationSection {
        // 这三项直接写入 treatment_information 表，保持蛇形字段映射
        @JsonProperty("surgery_method")
        private String surgeryMethod;
        @JsonProperty("other_surgery_method")
        private String otherSurgeryMethod;
        @JsonProperty("anesthesia_method")
        private String anesthesiaMethod;
    }
}
