package com.medical.service;

import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Patient;

public interface PatientService {

    Long add(Patient p);

    PatientDTO findById(Integer id);
}
