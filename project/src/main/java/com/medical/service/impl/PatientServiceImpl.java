package com.medical.service.impl;

import com.medical.mapper.PatientMapper;
import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Patient;
import com.medical.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<Patient> findAll() {
        return null;
    }



    @Override
    public void update(Patient p) {
//        dept.setUpdateTime(LocalDateTime.now());
//        deptMapper.update(dept);
    }
}
