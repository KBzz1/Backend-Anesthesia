    package com.medical.service.impl;

    import com.medical.pojo.DTO.RecoveryEventRecord;
    import com.medical.pojo.DTO.RecoveryEventRecord.IntraoperativeEvent;
    import com.medical.pojo.DTO.RecoveryEventRecord.ComplicationEvent;
    import com.medical.pojo.DTO.RecoveryEventRecord.MonitoringEvent;
    import com.medical.mapper.RERMapper;
    import com.medical.service.RERService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    public class RERServiceImpl implements RERService {

        @Autowired
        private RERMapper rerMapper;

        @Override
        public RecoveryEventRecord getRERByTreatmentId(Integer treatmentInformationId) {
            RecoveryEventRecord record = new RecoveryEventRecord();
            record.setTreatmentInformationId(treatmentInformationId);
            // 查询三类事件
            List<IntraoperativeEvent> intra = rerMapper.selectIntraoperativeEvents(treatmentInformationId);
            List<ComplicationEvent> comp = rerMapper.selectComplicationEvents(treatmentInformationId);
            MonitoringEvent monitoring = rerMapper.selectMonitoringEvent(treatmentInformationId);
            record.setIntraoperative(intra);
            record.setComplication(comp);
            record.setMonitoring(monitoring);
            return record;
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void saveRER(RecoveryEventRecord record) {
            if (record == null) {
                return;
            }
            Integer treatmentId = record.getTreatmentInformationId();
            if (treatmentId == null) {
                throw new IllegalArgumentException("treatmentInformationId is required");
            }

            MonitoringEvent m = record.getMonitoring();
            if (m != null) {
                rerMapper.insertMonitoringEvent(treatmentId, m.isAtomizationInhalation(), m.isOralCare(), m.isArteriovenousCatheterCare(),
                    m.isStSegmentAnalysis(), m.isHeartRateVariabilityAnalysis(), m.isBloodFluidWarmingTreatment(), m.isAuscultateBreathSounds(),
                    m.isLimbCompressionTherapy(), m.isMaskOxygenInhalation(), m.isOropharyngealNasopharyngealVentilation(), m.isLeaveRoomWithTube());
            }

            List<IntraoperativeEvent> intra = record.getIntraoperative();
            if (intra != null) {
                for (IntraoperativeEvent e : intra) {
                    String name = e.getEventName();
                    Integer eh = e.getEventHour();
                    Integer em = e.getEventMinute();
                    rerMapper.insertIntraoperativeEvent(treatmentId, name, eh, em);
                }
            }

            List<ComplicationEvent> comp = record.getComplication();
            if (comp != null) {
                for (ComplicationEvent c : comp) {
                    String name = c.getEventName();
                    Integer eh = c.getEventHour();
                    Integer em = c.getEventMinute();
                    if (eh == null || em == null) {
                        LocalDateTime now = LocalDateTime.now();
                        eh = now.getHour();
                        em = now.getMinute();
                    }
                    rerMapper.insertComplicationEvent(treatmentId, name, eh, em);
                }
            }
        }

    }
