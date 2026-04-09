package com.medical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.DTO.StatusDTO1;
import com.medical.pojo.DTO.StatusDTO2;
import com.medical.pojo.PatientStatus;
import com.medical.pojo.request.UpdateStatusRequest;
import com.medical.service.PatientStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientStatusControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PatientStatusService patientStatusService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        patientStatusService = mock(PatientStatusService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientStatusController(patientStatusService))
                .build();
    }

    @Test
    void updateStatusDelegatesToService() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setSurgeryId("15");
        request.setStatusCode(2);

        mockMvc.perform(post("/patients/status")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":null}"));

        verify(patientStatusService).updatePatientStatus("15", 2, true);
    }

    @Test
    void queryStatusReturnsEnvelope() throws Exception {
        when(patientStatusService.getPatientStatus("15")).thenReturn(new PatientStatus(4, 1763630609464L));

        mockMvc.perform(get("/patients/status/15"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "code": 1,
                          "msg": "success",
                          "data": {
                            "statusCode": 4,
                            "updateTime": 1763630609464
                          }
                        }
                        """));
    }

    @Test
    void queryPatientsReturnsEnvelope() throws Exception {
        StatusDTO1 dto = new StatusDTO1(1L, List.of(new StatusDTO1.PatientInfo(15L, "张三")));
        when(patientStatusService.getPatientInfoByStatus(4)).thenReturn(dto);

        mockMvc.perform(get("/patients/status/statusCode/4"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "code": 1,
                          "msg": "success",
                          "data": {
                            "totalCount": 1,
                            "patientList": [
                              {
                                "surgeryId": 15,
                                "patientName": "张三"
                              }
                            ]
                          }
                        }
                        """));
    }

    @Test
    void queryAllRegionReturnsEnvelope() throws Exception {
        when(patientStatusService.getAllRegionStatistics())
                .thenReturn(List.of(new StatusDTO2("恢复区", 1L, List.of())));

        mockMvc.perform(get("/patients/status/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "code": 1,
                          "msg": "success",
                          "data": [
                            {
                              "region": "恢复区",
                              "totalCount": 1,
                              "patientList": []
                            }
                          ]
                        }
                        """));
    }

    @Test
    void deleteStatusDelegatesToService() throws Exception {
        mockMvc.perform(delete("/patients/status/15"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":null}"));

        verify(patientStatusService).deletePatientStatus("15");
    }
}
