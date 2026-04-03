package com.medical.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.medical.pojo.DTO.AnesthesiaRecordSummaryDTO;
import com.medical.pojo.DTO.PatientSummaryDTO;
import com.medical.pojo.SurgeryStep;

@Mapper
public interface ARSMapper {

    List<PatientSummaryDTO> findPatientsByDate(LocalDate date);

    AnesthesiaRecordSummaryDTO findByTreatmentId(Long treatmentId);

    List<SurgeryStep> findSurgeryStepsByTreatmentId(Long treatmentId);

    Long findAnesthesiologistIdByTreatmentId(Long treatmentId);

    Long findRecoveryDoctorIdByTreatmentId(Long treatmentId);

    String findStaffNameById(Long staffId);

    List<Object> findSignatureById(Long staffId);
}
