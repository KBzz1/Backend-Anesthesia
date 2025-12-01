package com.medical.service;

import com.medical.pojo.Patient;
import com.medical.pojo.Surgery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SurgeryService {

    Long addRecord(Patient patient);

}
