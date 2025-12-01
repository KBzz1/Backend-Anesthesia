package com.medical.service;

import com.medical.pojo.Data;
import com.medical.pojo.Patient;
import org.springframework.stereotype.Service;

@Service
public interface DataService {

    void publish(String deviceId, Data data);

    void subscribe(String topic, String payload);

}
