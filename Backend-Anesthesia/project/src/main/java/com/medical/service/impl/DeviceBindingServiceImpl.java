package com.medical.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.BindingInfo;
import com.medical.pojo.Result;
import com.medical.service.DeviceBindingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class DeviceBindingServiceImpl implements DeviceBindingService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${device.binding.url:http://10.242.20.251:8080/device/binding/device/}")
    private String bindingUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BindingInfo getBindingInfo(String deviceId) {
        try {
            String url = bindingUrl + deviceId;
            log.info("Fetching binding info from: {}", url);
            Result result = restTemplate.getForObject(url, Result.class);

            if (result != null && result.getCode() == 1 && result.getData() != null) {
                // Convert LinkedHashMap to BindingInfo
                return objectMapper.convertValue(result.getData(), BindingInfo.class);
            }
        } catch (Exception e) {
            log.error("Error fetching device binding for deviceId: {}", deviceId, e);
        }
        return null;
    }
}
