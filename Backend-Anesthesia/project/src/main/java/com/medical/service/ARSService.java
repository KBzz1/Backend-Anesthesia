package com.medical.service;

import java.util.List;

import com.medical.pojo.DTO.AnesthesiaRecordSummaryDTO;
import com.medical.pojo.DTO.PatientSummaryDTO;

public interface ARSService {

    List<PatientSummaryDTO> getPatientsToday();

    AnesthesiaRecordSummaryDTO getByTreatmentId(Long treatmentId);

}
