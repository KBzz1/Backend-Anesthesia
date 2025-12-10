package com.medical.service;

import com.medical.pojo.Data;

public interface DataService {

    void publish(String deviceId, Data data);

}
