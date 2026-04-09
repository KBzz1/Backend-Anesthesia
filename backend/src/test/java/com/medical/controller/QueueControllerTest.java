package com.medical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.request.AppointmentCheckRequest;
import com.medical.pojo.request.AppointmentRequest;
import com.medical.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QueueControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        queueService = mock(QueueService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new QueueController(queueService)).build();
    }

    @Test
    void checkForAppointmentReturnsSuccessEnvelope() throws Exception {
        when(queueService.checkForAppointment(15L)).thenReturn("校验成功");

        mockMvc.perform(get("/queue/check/15"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":null}"));
    }

    @Test
    void appointmentReturnsCount() throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setSurgeryId(15L);
        request.setScheduledTime(LocalDateTime.of(2025, 11, 30, 8, 0, 0));
        when(queueService.appointment(15L, request.getScheduledTime())).thenReturn(2L);

        mockMvc.perform(put("/queue/appointment")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":2}"));
    }

    @Test
    void checkAppointmentCountReturnsCount() throws Exception {
        AppointmentCheckRequest request = new AppointmentCheckRequest();
        request.setScheduledTime(LocalDateTime.of(2025, 11, 30, 8, 0, 0));
        when(queueService.checkAppointmentCount(request.getScheduledTime())).thenReturn(2);

        mockMvc.perform(post("/queue/appointment/check")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":2}"));
    }

    @Test
    void registerReturnsErrorEnvelopeWhenUnbooked() throws Exception {
        when(queueService.register(15L)).thenReturn("未预约");

        mockMvc.perform(post("/queue/register/15"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":0,\"msg\":\"未预约\"}"));
    }

    @Test
    void missDelegatesToService() throws Exception {
        mockMvc.perform(post("/queue/miss/15"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":null}"));

        verify(queueService).miss(15L);
    }
}
