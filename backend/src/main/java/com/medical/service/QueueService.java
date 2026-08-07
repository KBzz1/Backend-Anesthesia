package com.medical.service;

import java.time.LocalDateTime;

public interface QueueService {

    String checkForAppointment(Long surgeryId);

    Long appointment(Long surgeryId, LocalDateTime scheduledTime);

    Integer checkAppointmentCount(LocalDateTime scheduledTime);

    String register(Long surgeryId);

    void miss(Long surgeryId);

    void syncAppointmentState(Long surgeryId, Integer newStatusCode);

    void clearAppointmentState(Long surgeryId);
}
