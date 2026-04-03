    package com.medical.mapper;

    import org.apache.ibatis.annotations.Mapper;
    import org.apache.ibatis.annotations.Param;

    @Mapper
    public interface RERMapper {
        // 查询术中事件列表
        java.util.List<com.medical.pojo.DTO.RecoveryEventRecord.IntraoperativeEvent> selectIntraoperativeEvents(@Param("treatmentInformationId") Integer treatmentInformationId);

        // 查询并发症事件列表
        java.util.List<com.medical.pojo.DTO.RecoveryEventRecord.ComplicationEvent> selectComplicationEvents(@Param("treatmentInformationId") Integer treatmentInformationId);

        // 查询监测事件（单条）
        com.medical.pojo.DTO.RecoveryEventRecord.MonitoringEvent selectMonitoringEvent(@Param("treatmentInformationId") Integer treatmentInformationId);

        void insertMonitoringEvent(@Param("treatmentInformationId") Integer treatmentInformationId,
                                   @Param("atomizationInhalation") boolean atomizationInhalation,
                                   @Param("oralCare") boolean oralCare,
                                   @Param("arteriovenousCatheterCare") boolean arteriovenousCatheterCare,
                                   @Param("stSegmentAnalysis") boolean stSegmentAnalysis,
                                   @Param("heartRateVariabilityAnalysis") boolean heartRateVariabilityAnalysis,
                                   @Param("bloodFluidWarmingTreatment") boolean bloodFluidWarmingTreatment,
                                   @Param("auscultateBreathSounds") boolean auscultateBreathSounds,
                                   @Param("limbCompressionTherapy") boolean limbCompressionTherapy,
                                   @Param("maskOxygenInhalation") boolean maskOxygenInhalation,
                                   @Param("oropharyngealNasopharyngealVentilation") boolean oropharyngealNasopharyngealVentilation,
                                   @Param("leaveRoomWithTube") boolean leaveRoomWithTube);

        void insertIntraoperativeEvent(@Param("treatmentInformationId") Integer treatmentInformationId,
                                       @Param("eventName") String eventName,
                                       @Param("eventHour") Integer eventHour,
                                       @Param("eventMinute") Integer eventMinute);

        void insertComplicationEvent(@Param("treatmentInformationId") Integer treatmentInformationId,
                                     @Param("eventName") String eventName,
                                     @Param("eventHour") Integer eventHour,
                                     @Param("eventMinute") Integer eventMinute);
    }
