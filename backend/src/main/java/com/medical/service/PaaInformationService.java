package com.medical.service;

import com.medical.pojo.DTO.PaaAssessmentRequest;
import com.medical.pojo.PaaInformation;

public interface PaaInformationService {

    void save(PaaInformation paaInformation);

    PaaInformation getBySurgeryId(Long surgeryId);

    /**
     * 将 patient、treatment_information、paa_information 三张表的数据一次性落库。
     * @return 新生成的 treatment_information_id（即 surgeryId）
     */
    Long saveAssessment(PaaAssessmentRequest request);

}
