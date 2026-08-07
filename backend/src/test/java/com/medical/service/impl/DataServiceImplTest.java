package com.medical.service.impl;

import com.medical.pojo.Data;
import com.medical.pojo.DTO.DeviceBindingDTO;
import com.medical.service.DeviceBindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataServiceImplTest {

    private SimpMessagingTemplate messagingTemplate;
    private DeviceBindingService deviceBindingService;
    private DataServiceImpl dataService;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        deviceBindingService = mock(DeviceBindingService.class);
        dataService = new DataServiceImpl(messagingTemplate, deviceBindingService);
    }

    @Test
    void publishPushesDeviceAndAggregateStreams() {
        DeviceBindingDTO bindingDTO = new DeviceBindingDTO();
        bindingDTO.setSurgeryId("15");
        when(deviceBindingService.getPatient("AA:BB")).thenReturn(bindingDTO);
        Data data = new Data();
        data.setHr(70);
        data.setTemp(36.5f);
        data.setResp(20);
        data.setBo(98);
        data.setTimestamp(123456789L);

        dataService.publish("AA:BB", data);

        verify(messagingTemplate).convertAndSend("/data/sub/AA:BB", data);
        verify(messagingTemplate).convertAndSend(
                org.mockito.Mockito.eq("/data/sub/all"),
                org.mockito.ArgumentMatchers.<Object>argThat(payload ->
                        payload instanceof java.util.Map<?, ?> map && "15".equals(map.get("surgeryId")))
        );
    }
}
