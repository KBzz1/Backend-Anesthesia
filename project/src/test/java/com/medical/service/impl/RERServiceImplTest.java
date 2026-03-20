package com.medical.service.impl;

import com.medical.mapper.RERMapper;
import com.medical.pojo.DTO.RecoveryEventRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RERServiceImplTest {

    @Mock
    private RERMapper rerMapper;

    @InjectMocks
    private RERServiceImpl rerService;

    @Test
    void saveRerPersistsComputedEventTime() {
        RecoveryEventRecord record = new RecoveryEventRecord();
        record.setTreatmentInformationId(12);

        RecoveryEventRecord.IntraoperativeEvent intraoperativeEvent = new RecoveryEventRecord.IntraoperativeEvent();
        intraoperativeEvent.setEventName("进入恢复室");
        intraoperativeEvent.setEventHour(8);
        intraoperativeEvent.setEventMinute(5);
        record.setIntraoperative(List.of(intraoperativeEvent));

        RecoveryEventRecord.ComplicationEvent complicationEvent = new RecoveryEventRecord.ComplicationEvent();
        complicationEvent.setEventName("恶心呕吐");
        complicationEvent.setEventHour(9);
        complicationEvent.setEventMinute(45);
        record.setComplication(List.of(complicationEvent));

        rerService.saveRER(record);

        verify(rerMapper).insertIntraoperativeEvent(12, "进入恢复室", 8, 5);
        verify(rerMapper).insertComplicationEvent(12, "恶心呕吐", 9, 45);
    }
}
