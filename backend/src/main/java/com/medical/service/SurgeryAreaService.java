package com.medical.service;

import com.medical.pojo.DTO.AnesthesiologistRequestDTO;
import com.medical.pojo.DTO.SurgeryAreaDTO;
import com.medical.pojo.DTO.SurgeryAreaRecordDTO;

public interface SurgeryAreaService {
    SurgeryAreaDTO getSurgeryAreaInfo(Long surgeryId);
    void saveSurgeryAreaRecord(Long surgeryId, SurgeryAreaRecordDTO recordDTO);
    String saveAnesthesiologist(AnesthesiologistRequestDTO requestDTO);
    String getSignatureOnly(Long staffId);
}
