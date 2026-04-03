package com.medical.service;

import com.medical.pojo.DTO.StatusDTO1;
import com.medical.pojo.DTO.StatusDTO2;
import com.medical.pojo.PatientStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PatientStatusService {

    /**
     * 更新患者状态
     */
    void updatePatientStatus(String surgeryId, Integer newStatusCode, boolean updateTimestamp);

    /**
     * 根据手术ID查询患者状态
     */
    PatientStatus getPatientStatus(String surgeryId);


    void pushAllRegionStatistics();
    List<StatusDTO2> getAllRegionStatistics();

    void pushPatientsByStatus(Integer statusCode);
    StatusDTO1 getPatientInfoByStatus(Integer statusCode);

    /**
     * 删除患者状态信息
     */
    public void deletePatientStatus(String surgeryId);

}
