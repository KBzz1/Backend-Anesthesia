package com.medical.controller;

import com.medical.pojo.DTO.PatientDTO;
import com.medical.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientControllerTest {

    private MockMvc mockMvc;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = mock(PatientService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PatientController(patientService)).build();
    }

    @Test
    void findByIdReturnsEnvelope() throws Exception {
        PatientDTO patient = new PatientDTO();
        patient.setPatientId(15);
        patient.setName("张三");
        patient.setGender("男");
        patient.setAge(30);
        patient.setIsSoldier(false);
        when(patientService.findById(15)).thenReturn(patient);

        mockMvc.perform(get("/patients/15"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "code": 1,
                          "msg": "success",
                          "data": {
                            "patientId": 15,
                            "name": "张三",
                            "gender": "男",
                            "age": 30,
                            "isSoldier": false
                          }
                        }
                        """));
    }
}
