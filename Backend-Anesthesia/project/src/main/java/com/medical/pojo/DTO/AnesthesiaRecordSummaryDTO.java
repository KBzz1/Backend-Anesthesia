package com.medical.pojo.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 麻醉记录单汇总版 DTO。
 * <p>
 * 目前只包含基本信息：姓名、性别、年龄、身份证号、术前诊断、拟行手术、术前特殊情况。
 * 字段命名尽量复用现有实体中的约定，便于 Mapper 复用。
 */
@Data
public class AnesthesiaRecordSummaryDTO {
        private java.util.List<com.medical.pojo.DTO.RecoveryEventRecord.IntraoperativeEvent> intraoperative;
        private java.util.List<com.medical.pojo.DTO.RecoveryEventRecord.ComplicationEvent> complication;
        private com.medical.pojo.DTO.RecoveryEventRecord.MonitoringEvent monitoring;
    public void setIntraoperative(java.util.List<com.medical.pojo.DTO.RecoveryEventRecord.IntraoperativeEvent> intraoperative) {
        this.intraoperative = intraoperative;
    }

    public void setComplication(java.util.List<com.medical.pojo.DTO.RecoveryEventRecord.ComplicationEvent> complication) {
        this.complication = complication;
    }

    public void setMonitoring(com.medical.pojo.DTO.RecoveryEventRecord.MonitoringEvent monitoring) {
        this.monitoring = monitoring;
    }

    // 基本信息：来自 patient / paa_information
    @JsonIgnore
    private Long patientId;
    private String name;
    private String gender;
    private Integer age;
    private String idCardNumber;

    // 术前诊断（可根据实际字段名在 Mapper 中进行映射）
    private String preoperativeDiagnosis;

    // 拟行手术
    private String plannedSurgery;

    // 麻醉方式
    private String anesthesiaMethod;

    // 术前特殊情况
    private String preoperativeSpecialCondition;

    // 麻醉医师信息
    private DoctorInfo anesthesiologist;
    // 恢复区医师信息
    private DoctorInfo recoveryDoctor;

    @Data
    public static class DoctorInfo {
        private String name;
        private String signature;
    }

    // 手术步骤记录
    private java.util.List<SurgeryRecordItem> surgeryRecord;

    // 用药推注记录
    private java.util.List<DrugRecordItemDTO> drugRecord;

    @Data
    public static class SurgeryRecordItem {
        private String eventName;
        private Integer eventHour;
        private Integer eventMinute;
    }

    // 恢复室评估小节，对应 recovery_area_room_assessment
    private RecoverySection recovery;


    @Data
    public static class RecoverySection {
        private Integer bp;
        private Integer pBpm;
        private Integer rBpm;
        private Integer spo2;

        private String anesthesiaSatisfaction;
        private Integer vasScore;
        private Boolean recoveryConscious;
        private String skinCondition;
        private Integer stewardScore;
        private String awakeningLevel;
        private String airwayPatency;
        private String limbActivity;
        private String pupilLightReflex;
        private Integer respirationVt;
        private Integer muscleStrength;
        private Integer topRatio;
        private String respirationSound;
        private String reflex;
        private String sound;
        private String selfReportAbility;

        private Boolean consciousnessOrientation;
        private Boolean spatialOrientation;
        private Boolean calculationAbility;
        private Boolean memory;
        private String pupilEqual;
    }

    // 手术时间信息
    private java.time.OffsetDateTime surgeryStartTime;
    private java.time.OffsetDateTime surgeryEndTime;
    private java.time.Instant recoveryEndTime;
}
