package com.medical.service.impl;

import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.PatientStatus;
import com.medical.service.PatientStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class QueueServiceImplTest {

    private SurgeryMapper surgeryMapper;
    private PatientStatusService patientStatusService;
    private StringRedisTemplate stringRedisTemplate;
    private QueueServiceImpl queueService;

    @BeforeEach
    void setUp() {
        surgeryMapper = mock(SurgeryMapper.class);
        patientStatusService = mock(PatientStatusService.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        queueService = new QueueServiceImpl(surgeryMapper, patientStatusService, stringRedisTemplate);
    }

    @Test
    void checkForAppointmentReturnsUnevaluatedWhenStatusMissing() {
        when(patientStatusService.getPatientStatus("15")).thenReturn(null);

        String result = queueService.checkForAppointment(15L);

        assertEquals("未麻醉评估", result);
        verify(surgeryMapper).getRecord(15L);
    }

    @Test
    void registerReturnsUnbookedWhenOnlyEvaluated() {
        when(patientStatusService.getPatientStatus("15")).thenReturn(new PatientStatus(1, 1L));

        String result = queueService.register(15L);

        assertEquals("未预约", result);
        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    void registerUpdatesStatusWhenBooked() {
        when(patientStatusService.getPatientStatus("15")).thenReturn(new PatientStatus(2, 1L));

        String result = queueService.register(15L);

        assertEquals("签到成功", result);
        verify(patientStatusService).updatePatientStatus("15", 3, true);
    }
}
