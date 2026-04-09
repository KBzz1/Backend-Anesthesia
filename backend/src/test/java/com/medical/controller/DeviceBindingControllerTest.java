package com.medical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.DTO.DeviceBindingDTO;
import com.medical.pojo.request.BindDeviceRequest;
import com.medical.service.DeviceBindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeviceBindingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DeviceBindingService deviceBindingService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        deviceBindingService = mock(DeviceBindingService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DeviceBindingController(deviceBindingService))
                .build();
    }

    @Test
    void bindDeviceDelegatesToService() throws Exception {
        BindDeviceRequest request = new BindDeviceRequest();
        request.setSurgeryId("15");
        request.setMacAddress("AA:BB:CC:DD:EE:FF");

        mockMvc.perform(post("/device/binding")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":null}"));

        verify(deviceBindingService).bindDevice("15", "AA:BB:CC:DD:EE:FF");
    }

    @Test
    void getDeviceBindingInfoReturnsEnvelope() throws Exception {
        DeviceBindingDTO dto = new DeviceBindingDTO();
        dto.setSurgeryId("15");
        dto.setBindTime(1763652769468L);
        when(deviceBindingService.getPatient("AA:BB")).thenReturn(dto);

        mockMvc.perform(get("/device/binding/device/AA:BB"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "code": 1,
                          "msg": "success",
                          "data": {
                            "surgeryId": "15",
                            "bindTime": 1763652769468
                          }
                        }
                        """));
    }

    @Test
    void getBindingStatisticsReturnsEnvelope() throws Exception {
        when(deviceBindingService.getBindingStatistics()).thenReturn(Map.of("AA:BB", "15"));

        mockMvc.perform(get("/device/binding/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "code": 1,
                          "msg": "success",
                          "data": {
                            "AA:BB": "15"
                          }
                        }
                        """));
    }

    @Test
    void unbindDeviceDelegatesToService() throws Exception {
        mockMvc.perform(delete("/device/binding/device/AA:BB"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1,\"msg\":\"success\",\"data\":null}"));

        verify(deviceBindingService).unbindDevice("AA:BB");
    }
}
