package com.medical.service;

import com.medical.pojo.PaaInformation;
import org.springframework.stereotype.Service;

@Service
public interface PaaInformationService {

    void save(PaaInformation paaInformation);

    PaaInformation getByTreatmentInformationId(Long treatmentInformationId);

}
