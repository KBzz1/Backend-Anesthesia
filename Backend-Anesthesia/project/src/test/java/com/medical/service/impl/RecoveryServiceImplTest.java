package com.medical.service.impl;

import com.medical.mapper.RecoveryMapper;
import com.medical.pojo.Recovery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecoveryServiceImplTest {

    @Mock
    private RecoveryMapper recoveryMapper;

    @InjectMocks
    private RecoveryServiceImpl recoveryService;

    @Test
    void saveAssessmentUpdatesRecoveryEndTime() {
        Recovery recovery = new Recovery();
        recovery.setTreatmentInformationId(23);

        recoveryService.saveAssessment(recovery);

        verify(recoveryMapper).insertAssessment(recovery);
        verify(recoveryMapper).updateRecoveryEndTime(23);
    }
}
