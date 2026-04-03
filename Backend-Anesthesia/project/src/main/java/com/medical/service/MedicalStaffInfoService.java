package com.medical.service;

import com.medical.pojo.DTO.StaffInfoDTO;

public interface MedicalStaffInfoService {
    StaffInfoDTO getStaffInfo(Long staffId);
}
