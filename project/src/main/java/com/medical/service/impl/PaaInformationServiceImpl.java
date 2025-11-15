package com.medical.service.impl;

import com.medical.mapper.PaaInformationMapper;
import com.medical.pojo.PaaInformation;
import com.medical.service.PaaInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaaInformationServiceImpl implements PaaInformationService {

    @Autowired
    private PaaInformationMapper paaInformationMapper;

    @Override
    public void save(PaaInformation paaInformation) {
        paaInformationMapper.insert(paaInformation);
    }

    @Override
    public PaaInformation getByTreatmentInformationId(Long treatmentInformationId) {
        return paaInformationMapper.getByTreatmentInformationId(treatmentInformationId);
    }
}
