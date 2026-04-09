package com.medical.service.impl;

import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.DTO.DeviceBindingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceBindingServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private SurgeryMapper surgeryMapper;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private DeviceBindingServiceImpl deviceBindingService;

    @BeforeEach
    void setUp() {
        deviceBindingService = new DeviceBindingServiceImpl(stringRedisTemplate, surgeryMapper);
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void bindDeviceStoresBidirectionalBindingAndPersistsBindTime() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(hashOperations.get("device:binding:AA:BB", "surgeryId")).thenReturn(null);
        when(valueOperations.get("patient:device:15")).thenReturn(null);

        deviceBindingService.bindDevice("15", "AA:BB");

        verify(hashOperations).putAll(
                eq("device:binding:AA:BB"),
                argThat(hasBindingInfo("15"))
        );
        verify(valueOperations).set("patient:device:15", "AA:BB");
        verify(setOperations).add("device:bound:set", "AA:BB");
        verify(surgeryMapper).updateDeviceBindTime(eq(15L), any(LocalDateTime.class));
    }

    @Test
    void getPatientReturnsDtoFromBindingHash() {
        when(hashOperations.entries("device:binding:AA:BB"))
                .thenReturn(Map.of("surgeryId", "15", "bindTime", "123456"));

        DeviceBindingDTO result = deviceBindingService.getPatient("AA:BB");

        assertEquals("15", result.getSurgeryId());
        assertEquals(123456L, result.getBindTime());
    }

    @Test
    void getPatientReturnsNullWhenBindingMissing() {
        when(hashOperations.entries("device:binding:AA:BB")).thenReturn(Map.of());

        assertNull(deviceBindingService.getPatient("AA:BB"));
    }

    private ArgumentMatcher<Map<String, String>> hasBindingInfo(String surgeryId) {
        return bindingInfo -> surgeryId.equals(bindingInfo.get("surgeryId"))
                && bindingInfo.get("bindTime") != null
                && !bindingInfo.get("bindTime").isBlank();
    }
}
