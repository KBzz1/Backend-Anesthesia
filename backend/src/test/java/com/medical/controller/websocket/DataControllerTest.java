package com.medical.controller.websocket;

import com.medical.pojo.Data;
import com.medical.service.DataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DataControllerTest {

    private DataService dataService;
    private SimpMessagingTemplate messagingTemplate;
    private DataController dataController;

    @BeforeEach
    void setUp() {
        dataService = mock(DataService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        dataController = new DataController(dataService, messagingTemplate);
    }

    @Test
    void receiveDataDelegatesToService() {
        Data data = new Data();
        data.setHr(75);
        data.setTemp(36.6f);

        dataController.receiveData("AA:BB", data);

        verify(dataService).publish("AA:BB", data);
    }

    @Test
    void flushAckCountsPublishesAggregateAck() {
        Data data = new Data();
        data.setHr(75);
        data.setTemp(36.6f);
        dataController.receiveData("AA:BB", data);

        dataController.flushAckCounts();

        verify(messagingTemplate).convertAndSend(org.mockito.Mockito.eq("/data/pub/response"), org.mockito.ArgumentMatchers.contains("\"count\":1"));
    }
}
