package com.medical.service;

import com.medical.pojo.DTO.StatusDTO1;
import com.medical.pojo.DTO.StatusDTO2;
import com.medical.pojo.PatientStatus;

import java.util.List;

public interface PatientStatusService {

    void updatePatientStatus(String surgeryId, Integer newStatusCode, boolean updateTimestamp);

    PatientStatus getPatientStatus(String surgeryId);

    void pushAllRegionStatistics();

    List<StatusDTO2> getAllRegionStatistics();

    void pushPatientsByStatus(Integer statusCode);

    StatusDTO1 getPatientInfoByStatus(Integer statusCode);

    void deletePatientStatus(String surgeryId);
}
