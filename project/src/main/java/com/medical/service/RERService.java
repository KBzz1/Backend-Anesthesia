package com.medical.service;

import com.medical.pojo.DTO.RecoveryEventRecord;

public interface RERService {
    void saveRER(RecoveryEventRecord record);

    RecoveryEventRecord getRERByTreatmentId(Integer treatmentInformationId);
}
