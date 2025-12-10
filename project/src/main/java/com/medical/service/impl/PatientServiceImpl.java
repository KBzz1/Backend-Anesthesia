package com.medical.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medical.mapper.PatientMapper;
import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Patient;
import com.medical.service.PatientService;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public PatientDTO findById(Integer id) {
        return patientMapper.findById(id);
    }

    @Override
    public Long add(Patient patient) {
        return patientMapper.add(patient);
    }
}
