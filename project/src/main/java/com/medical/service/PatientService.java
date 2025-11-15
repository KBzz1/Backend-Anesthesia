package com.medical.service;

import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Patient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PatientService {

    List<Patient> findAll();

    Long add(Patient p);

    PatientDTO findById(Integer id);

    void update(Patient p);
}
