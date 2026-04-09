package com.medical.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medical.mapper.MedicalStaffInfoMapper;
import com.medical.pojo.DTO.StaffInfoDTO;
import com.medical.pojo.MedicalStaffInfo;
import com.medical.service.MedicalStaffInfoService;

@Service
public class MedicalStaffInfoServiceImpl implements MedicalStaffInfoService {

    @Autowired
    private MedicalStaffInfoMapper medicalStaffInfoMapper;

    @Override
    public StaffInfoDTO getStaffInfo(Long staffId) {
        MedicalStaffInfo staff = medicalStaffInfoMapper.getStaffById(staffId);
        if (staff != null) {
            return new StaffInfoDTO(staff.getName(), staff.getPosition(), staff.getGender());
        }
        return null;
    }
}
