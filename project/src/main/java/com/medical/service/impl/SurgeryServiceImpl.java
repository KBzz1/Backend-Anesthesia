package com.medical.service.impl;

import com.medical.mapper.PatientMapper;
import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.Patient;
import com.medical.pojo.Surgery;
import com.medical.service.PatientService;
import com.medical.service.SurgeryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SurgeryServiceImpl implements SurgeryService {

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private SurgeryMapper surgeryMapper;


    @Transactional
    public Long addRecord(Patient patient) {
        patientMapper.add(patient);
        Surgery info = new Surgery();
        info.setPatientId(patient.getPatientId().longValue());
        surgeryMapper.addRecord(info);
        return info.getSurgeryId();
    }
}
