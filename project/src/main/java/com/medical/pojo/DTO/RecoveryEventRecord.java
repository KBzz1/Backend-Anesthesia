package com.medical.pojo.DTO;

import lombok.Data;
import java.util.List;

/**
 * 恢复区事件总数据 DTO
 * 包含：
 * 1. 术中事件（intraoperative）
 * 2. 并发症事件（complication）
 * 3. 监测事件（monitoring）
 */
@Data
public class RecoveryEventRecord {

    /** 治疗信息ID，对应 treatment_information_id */
    private Integer treatmentInformationId;

    /** 术中事件（每种仅记录一次，但可能包含数值，如 ml、mg、次/分） */
    private List<IntraoperativeEvent> intraoperative;

    /** 并发症事件（可重复发生多个） */
    private List<ComplicationEvent> complication;

    /** 监测事件（布尔开关记录，仅一条） */
    private MonitoringEvent monitoring;


    // ----------------------------------------------
    // 术中事件对象
    // ----------------------------------------------
    @Data
    public static class IntraoperativeEvent {
        /** 事件名称，例如“进入恢复室”、“拔管”、“林格”等 */
        private String eventName;

        /** 事件小时 */
        private Integer eventHour;

        /** 事件分钟 */
        private Integer eventMinute;

    }


    // ----------------------------------------------
    // 并发症事件对象
    // ----------------------------------------------
    @Data
    public static class ComplicationEvent {
        /** 并发症事件名称，例如“恶心呕吐”、“剧烈体动”等 */
        private String eventName;

        /** 事件小时 */
        private Integer eventHour;

        /** 事件分钟 */
        private Integer eventMinute;
    }


    // ----------------------------------------------
    // 监测事件对象，对应数据库 recovery_monitoring_event
    // ----------------------------------------------
    @Data
    public static class MonitoringEvent {
        /** 雾化吸入 atomization_inhalation */
        private boolean atomizationInhalation;

        /** 口腔护理 oral_care */
        private boolean oralCare;

        /** 动静脉置管护理 arteriovenous_catheter_care */
        private boolean arteriovenousCatheterCare;

        /** ST 段分析 st_segment_analysis */
        private boolean stSegmentAnalysis;

        /** 心率变异性分析 heart_rate_variability_analysis */
        private boolean heartRateVariabilityAnalysis;

        /** 血液/液体加温治疗 blood_fluid_warming_treatment */
        private boolean bloodFluidWarmingTreatment;

        /** 听诊呼吸音 auscultate_breath_sounds */
        private boolean auscultateBreathSounds;

        /** 肢体气压治疗 limb_compression_therapy */
        private boolean limbCompressionTherapy;

        /** 面罩吸氧 mask_oxygen_inhalation */
        private boolean maskOxygenInhalation;

        /** 口咽/鼻咽通气 oropharyngeal_nasopharyngeal_ventilation */
        private boolean oropharyngealNasopharyngealVentilation;

        /** 带管出室 leaveRoomWithTube */
        private boolean leaveRoomWithTube;
    }

}
