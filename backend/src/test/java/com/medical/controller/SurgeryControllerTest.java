package com.medical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.Patient;
import com.medical.service.SurgeryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SurgeryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SurgeryService surgeryService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        surgeryService = mock(SurgeryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SurgeryController(surgeryService)).build();
    }

    @Test
    void addRecordReturnsEnvelopeWithSurgeryId() throws Exception {
        Patient patient = new Patient();
        patient.setName("张三");
        patient.setGender("男");
        patient.setAge(30);
        patient.setIsSoldier(false);
        when(surgeryService.addRecord(org.mockito.ArgumentMatchers.any(Patient.class))).thenReturn(15L);

        mockMvc.perform(post("/surgery")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":15}"));
    }
}
