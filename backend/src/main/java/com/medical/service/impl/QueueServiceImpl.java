package com.medical.service.impl;

import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.PatientStatus;
import com.medical.service.PatientStatusService;
import com.medical.service.QueueService;
import com.medical.utils.constants.PatientFlowRedisKeys;
import com.medical.utils.constants.QueueConstants;
import com.medical.utils.constants.StatusConstants;
import com.medical.utils.constants.SuperPatientConstants;
import com.medical.utils.constants.SuperPatientPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class QueueServiceImpl implements QueueService {

    private static final DateTimeFormatter APPOINTMENT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SurgeryMapper surgeryMapper;
    private final PatientStatusService patientStatusService;
    private final StringRedisTemplate stringRedisTemplate;

    public QueueServiceImpl(
            SurgeryMapper surgeryMapper,
            PatientStatusService patientStatusService,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.surgeryMapper = surgeryMapper;
        this.patientStatusService = patientStatusService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String checkForAppointment(Long surgeryId) {
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            surgeryMapper.getRecord(surgeryId);
        }
        PatientStatus patientStatus = patientStatusService.getPatientStatus(String.valueOf(surgeryId));
        if (patientStatus == null) {
            return QueueConstants.UNEVALUATED;
        }
        if (patientStatus.getStatusCode() >= 2) {
            return QueueConstants.BOOKED;
        }
        return QueueConstants.BOOK_SUCCESS;
    }

    @Override
    public Long appointment(Long surgeryId, LocalDateTime scheduledTime) {
        if (scheduledTime == null) {
            throw new IllegalArgumentException("预约时间不能为空");
        }
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            LocalDateTime appointmentRequestTime = LocalDateTime.now(ZoneId.of("GMT+8"));
            surgeryMapper.updateScheduleTime(surgeryId, appointmentRequestTime, scheduledTime);
        } else {
            updateSuperPatientAppointmentSlot(surgeryId, scheduledTime);
        }

        patientStatusService.updatePatientStatus(String.valueOf(surgeryId), StatusConstants.APPOINTED, true);
        if (SuperPatientConstants.isSuperPatient(surgeryId)) {
            String count = stringRedisTemplate.opsForValue().get(demoAppointmentCountKey(scheduledTime));
            return count != null ? Long.parseLong(count) : 0L;
        }

        String redisKey = appointmentCountKey(scheduledTime);
        Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);
        if (currentCount == 1) {
            stringRedisTemplate.expire(redisKey, 7, TimeUnit.DAYS);
        }
        return currentCount;
    }

    @Override
    public Integer checkAppointmentCount(LocalDateTime scheduledTime) {
        String redisKey = appointmentCountKey(scheduledTime);
        String count = stringRedisTemplate.opsForValue().get(redisKey);
        return count != null ? Integer.parseInt(count) : 0;
    }

    @Override
    public String register(Long surgeryId) {
        PatientStatus patientStatus = patientStatusService.getPatientStatus(String.valueOf(surgeryId));
        if (patientStatus == null) {
            return QueueConstants.UNEVALUATED;
        }
        int statusCode = patientStatus.getStatusCode();

        Integer superResult = SuperPatientPolicy.registerAsSuperPatient(surgeryId, statusCode, patientStatusService);
        if (superResult != null) {
            // SuperPatient 已处理：若是从 MISSED 刚更新到 CHECKED_IN 返回成功，否则返回已签到
            return superResult == StatusConstants.CHECKED_IN
                    ? QueueConstants.REGISTER_SUCCESS
                    : QueueConstants.REGISTERED;
        }

        if (statusCode == StatusConstants.EVALUATED) {
            return QueueConstants.UNBOOKED;
        } else if (statusCode >= StatusConstants.CHECKED_IN && statusCode != StatusConstants.MISSED) {
            return QueueConstants.REGISTERED;
        }
        patientStatusService.updatePatientStatus(
                String.valueOf(surgeryId),
                StatusConstants.CHECKED_IN,
                true
        );
        return QueueConstants.REGISTER_SUCCESS;
    }

    @Override
    public void miss(Long surgeryId) {
        PatientStatus patientStatus = patientStatusService.getPatientStatus(String.valueOf(surgeryId));
        if (patientStatus == null) {
            throw new IllegalArgumentException("患者不存在");
        }
        if (patientStatus.getStatusCode() != StatusConstants.CHECKED_IN) {
            throw new IllegalArgumentException("只有已签到状态的患者才能过号！当前状态: " + patientStatus.getStatusCode());
        }
        patientStatusService.updatePatientStatus(String.valueOf(surgeryId), StatusConstants.MISSED, false);
        log.info("患者过号: surgeryId={}, 状态变更为过号", surgeryId);
    }

    @Override
    public void syncAppointmentState(Long surgeryId, Integer newStatusCode) {
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            return;
        }
        String scheduledTime = stringRedisTemplate.opsForValue().get(appointmentSurgeryKey(surgeryId));
        if (scheduledTime == null || scheduledTime.isBlank()) {
            return;
        }
        if (newStatusCode != null && newStatusCode == StatusConstants.APPOINTED) {
            ensureSuperPatientSlotReservation(surgeryId, scheduledTime);
            return;
        }
        releaseSuperPatientAppointmentSlot(surgeryId, scheduledTime);
    }

    @Override
    public void clearAppointmentState(Long surgeryId) {
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            return;
        }
        String appointmentKey = appointmentSurgeryKey(surgeryId);
        String scheduledTime = stringRedisTemplate.opsForValue().get(appointmentKey);
        if (scheduledTime != null && !scheduledTime.isBlank()) {
            releaseSuperPatientAppointmentSlot(surgeryId, scheduledTime);
        }
        stringRedisTemplate.delete(appointmentKey);
    }

    private void updateSuperPatientAppointmentSlot(Long surgeryId, LocalDateTime scheduledTime) {
        String appointmentKey = appointmentSurgeryKey(surgeryId);
        String newScheduledTime = formatScheduledTime(scheduledTime);
        String oldScheduledTime = stringRedisTemplate.opsForValue().get(appointmentKey);
        if (oldScheduledTime != null && !oldScheduledTime.equals(newScheduledTime)) {
            releaseSuperPatientAppointmentSlot(surgeryId, oldScheduledTime);
        }
        stringRedisTemplate.opsForValue().set(appointmentKey, newScheduledTime);
        ensureSuperPatientSlotReservation(surgeryId, newScheduledTime);
    }

    private void ensureSuperPatientSlotReservation(Long surgeryId, String scheduledTime) {
        String slotKey = appointmentSlotKey(scheduledTime);
        Boolean member = stringRedisTemplate.opsForSet().isMember(slotKey, String.valueOf(surgeryId));
        if (Boolean.TRUE.equals(member)) {
            return;
        }
        stringRedisTemplate.opsForSet().add(slotKey, String.valueOf(surgeryId));
        Long count = stringRedisTemplate.opsForValue().increment(demoAppointmentCountKey(scheduledTime));
        if (count == 1) {
            stringRedisTemplate.expire(slotKey, 7, TimeUnit.DAYS);
            stringRedisTemplate.expire(demoAppointmentCountKey(scheduledTime), 7, TimeUnit.DAYS);
        }
    }

    private void releaseSuperPatientAppointmentSlot(Long surgeryId, String scheduledTime) {
        String slotKey = appointmentSlotKey(scheduledTime);
        Long removed = stringRedisTemplate.opsForSet().remove(slotKey, String.valueOf(surgeryId));
        if (removed == null || removed == 0) {
            return;
        }
        String countKey = demoAppointmentCountKey(scheduledTime);
        Long count = stringRedisTemplate.opsForValue().decrement(countKey);
        if (count != null && count <= 0) {
            stringRedisTemplate.delete(countKey);
            stringRedisTemplate.delete(slotKey);
        }
    }

    private String appointmentCountKey(LocalDateTime scheduledTime) {
        return PatientFlowRedisKeys.APPOINTMENT_COUNT_PREFIX + formatScheduledTime(scheduledTime);
    }

    private String demoAppointmentCountKey(LocalDateTime scheduledTime) {
        return demoAppointmentCountKey(formatScheduledTime(scheduledTime));
    }

    private String demoAppointmentCountKey(String scheduledTime) {
        return PatientFlowRedisKeys.DEMO_APPOINTMENT_COUNT_PREFIX + scheduledTime;
    }

    private String appointmentSlotKey(String scheduledTime) {
        return PatientFlowRedisKeys.DEMO_APPOINTMENT_SLOT_PREFIX + scheduledTime;
    }

    private String appointmentSurgeryKey(Long surgeryId) {
        return PatientFlowRedisKeys.DEMO_APPOINTMENT_SURGERY_PREFIX + surgeryId;
    }

    private String formatScheduledTime(LocalDateTime scheduledTime) {
        return scheduledTime.format(APPOINTMENT_FORMATTER);
    }
}
