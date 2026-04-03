package com.medical.service.impl;

import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.PatientStatus;
import com.medical.service.PatientStatusService;
import com.medical.service.QueueService;
import com.medical.utils.constants.QueueConstants;
import com.medical.utils.constants.RedisConstants;
import com.medical.utils.constants.StatusConstants;
import com.medical.utils.constants.SuperPatientConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class QueueServiceImpl implements QueueService {

    private static final DateTimeFormatter APPOINTMENT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SurgeryMapper surgeryMapper;
    @Autowired
    private PatientStatusService patientStatusService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final Set<Long> missedIds = ConcurrentHashMap.newKeySet();

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
        // 暂时默认已经缴费，缴费信息还无法判断
//        if (!surgery.getIsPaid()) {
//            return QueueConstants.UNPAID;
//        }
        // 评估完且已缴费
        return QueueConstants.BOOK_SUCCESS;
    }

    public Long appointment(Long surgeryId, LocalDateTime scheduledTime) {
        if (scheduledTime == null) {
            throw new IllegalArgumentException("预约时间不能为空");
        }
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            // 生成当前时间作为预约请求时间
            LocalDateTime appointmentRequestTime = LocalDateTime.now();
            surgeryMapper.updateScheduleTime(
                    surgeryId,
                    appointmentRequestTime,
                    scheduledTime
            );
        } else {
            updateSuperPatientAppointmentSlot(surgeryId, scheduledTime);
        }
        // 预约成功则变更患者信息
        patientStatusService.updatePatientStatus(String.valueOf(surgeryId), StatusConstants.APPOINTED, true);
        if (SuperPatientConstants.isSuperPatient(surgeryId)) {
            String count = stringRedisTemplate.opsForValue().get(demoAppointmentCountKey(scheduledTime));
            return count != null ? Long.parseLong(count) : 0L;
        }
        // 记录预约人数
        String redisKey = appointmentCountKey(scheduledTime);
        Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);
        if (currentCount == 1) {
            stringRedisTemplate.expire(redisKey, 7, TimeUnit.DAYS);
        }
        return currentCount;
    }

    public Integer checkAppointmentCount(LocalDateTime scheduledTime) {
        String redisKey = appointmentCountKey(scheduledTime);
        String count = stringRedisTemplate.opsForValue().get(redisKey);

        return count != null ? Integer.parseInt(count) : 0;
    }

    // 签到
    public String register(Long surgeryId) {
        // 判定：未评估、未预约，已签到
        // 不在预约时段的判别暂不设置
        PatientStatus patientStatus = patientStatusService.getPatientStatus(String.valueOf(surgeryId));
        if (patientStatus == null) {
            return QueueConstants.UNEVALUATED;
        }
        int statusCode = patientStatus.getStatusCode();
        if (statusCode == 1) {
            return QueueConstants.UNBOOKED; // 未预约
        } else if (statusCode >= 3) {
            return QueueConstants.REGISTERED; // 已签到
        }
        // 可以签到了，将患者状态变更为已签到（特殊id不更新时间状态，可以插队）
        patientStatusService.updatePatientStatus(
                String.valueOf(surgeryId),
                StatusConstants.CHECKED_IN,
                !missedIds.contains(surgeryId)
        );
        return QueueConstants.REGISTER_SUCCESS;
    }

    // 签到
    public void miss(Long surgeryId) {
        // 获取当前状态
        PatientStatus patientStatus = patientStatusService.getPatientStatus(String.valueOf(surgeryId));
        if (patientStatus == null) {
            throw new IllegalArgumentException("患者不存在");
        }
        // 只有已签到状态（3）才能过号
        if (patientStatus.getStatusCode() != StatusConstants.CHECKED_IN) {
            throw new IllegalArgumentException("只有已签到状态的患者才能过号！当前状态: " + patientStatus.getStatusCode());
        }
        // 状态回退为 2（已预约），同时ids保持为特殊id，下次状态变更为签到时则不更改时间戳
        patientStatusService.updatePatientStatus(String.valueOf(surgeryId), StatusConstants.APPOINTED, false);
        missedIds.add(surgeryId);
        log.info("患者过号: surgeryId={}, 已加入过号集合", surgeryId);
    }

    // 清除过号标记（当患者进入备室时调用）
    public void clearMissedID(Long surgeryId) {
        if (missedIds.remove(surgeryId)) {
            log.info("清除过号标记: surgeryId={}", surgeryId);
        }
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
        return RedisConstants.APPOINTMENT_COUNT_PREFIX + formatScheduledTime(scheduledTime);
    }

    private String appointmentCountKey(String scheduledTime) {
        return RedisConstants.APPOINTMENT_COUNT_PREFIX + scheduledTime;
    }

    private String demoAppointmentCountKey(LocalDateTime scheduledTime) {
        return demoAppointmentCountKey(formatScheduledTime(scheduledTime));
    }

    private String demoAppointmentCountKey(String scheduledTime) {
        return RedisConstants.DEMO_APPOINTMENT_COUNT_PREFIX + scheduledTime;
    }

    private String appointmentSlotKey(String scheduledTime) {
        return RedisConstants.DEMO_APPOINTMENT_SLOT_PREFIX + scheduledTime;
    }

    private String appointmentSurgeryKey(Long surgeryId) {
        return RedisConstants.DEMO_APPOINTMENT_SURGERY_PREFIX + surgeryId;
    }

    private String formatScheduledTime(LocalDateTime scheduledTime) {
        return scheduledTime.format(APPOINTMENT_FORMATTER);
    }

}
