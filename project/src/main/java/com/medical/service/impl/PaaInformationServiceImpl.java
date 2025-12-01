package com.medical.service.impl;

import com.medical.mapper.PaaInformationMapper;
import com.medical.mapper.PatientMapper;
import com.medical.mapper.SurgeryMapper;
import com.medical.pojo.DTO.PaaAssessmentRequest;
import com.medical.pojo.PaaInformation;
import com.medical.pojo.Patient;
import com.medical.pojo.Surgery;
import com.medical.service.PaaInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaaInformationServiceImpl implements PaaInformationService {

    @Autowired
    private PaaInformationMapper paaInformationMapper;
    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private SurgeryMapper surgeryMapper;

    @Override
    public void save(PaaInformation paaInformation) {
        paaInformationMapper.insert(paaInformation);
    }

    @Override
    public PaaInformation getBySurgeryId(Long surgeryId) {
        return paaInformationMapper.getBySurgeryId(surgeryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAssessment(PaaAssessmentRequest request) {
        // 1. 先写 patient，获取 patient_id
        Patient persistedPatient = createPatient(request.getPatient());
        // 2. 写 treatment_information ，拿到 surgeryId 作为后续外键
        Surgery surgery = createSurgery(request.getTreatmentInformation(), persistedPatient);
        // 3. 为 PAA 实体补齐外键后插入
        PaaInformation paaInformation = attachPaaInformation(request.getPaa(), surgery);
        paaInformationMapper.insert(paaInformation);
        // 对前端暴露 treatment_information_id（即 surgeryId）作为后续关联主键
        return surgery.getSurgeryId();
    }

    // 构造并持久化 patient 记录
    private Patient createPatient(PaaAssessmentRequest.PatientSection payload) {
        Patient patient = new Patient();
        if (payload != null) {
            patient.setName(payload.getName());
            patient.setGender(payload.getGender());
            patient.setAge(payload.getAge());
            patient.setIsSoldier(payload.getIsSoldier());
            patient.setIdCardNumber(payload.getIdCardNumber());
        }
        patientMapper.add(patient);
        return patient;
    }

    // 将 treatmentInformation 段写入 treatment_information 表
    private Surgery createSurgery(PaaAssessmentRequest.TreatmentInformationSection payload, Patient patient) {
        Surgery surgery = new Surgery();
        surgery.setPatientId(patient.getPatientId() == null ? null : patient.getPatientId().longValue());
        if (payload != null) {
            surgery.setSurgeryMethod(payload.getSurgeryMethod());
            surgery.setOtherSurgeryMethod(payload.getOtherSurgeryMethod());
            surgery.setAnesthesiaMethod(payload.getAnesthesiaMethod());
        }
        surgeryMapper.addRecord(surgery);
        return surgery;
    }

    // 给传入的 PAA 实体补齐 surgeryId，没有传时默认新实例
    private PaaInformation attachPaaInformation(PaaInformation paaPayload, Surgery surgery) {
        PaaInformation paaInformation = paaPayload;
        if (paaInformation == null) {
            paaInformation = new PaaInformation();
        }
        paaInformation.setSurgeryId(surgery.getSurgeryId());
        return paaInformation;
    }
}
