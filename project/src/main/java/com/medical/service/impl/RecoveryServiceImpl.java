package com.medical.service.impl;

import com.medical.mapper.RecoveryMapper;
import com.medical.pojo.Recovery;
import com.medical.service.RecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecoveryServiceImpl implements RecoveryService {

    @Autowired
    private RecoveryMapper recoveryMapper;

    @Override
    public Recovery save(Recovery recovery) {
        // 入室记录：插入 recovery_room_record 表
        recoveryMapper.insertRoomRecord(recovery);
        if (recovery != null && recovery.getTreatmentInformationId() != null && recovery.getStaffId() != null) {
            recoveryMapper.updateRecoveryDoctorId(recovery.getTreatmentInformationId(), recovery.getStaffId());
        }
        return recovery;
    }

    @Override
    public Recovery saveAssessment(Recovery recovery) {
        // 出室评估：插入 recovery_area_room_assessment 表
        recoveryMapper.insertAssessment(recovery);
        if (recovery != null && recovery.getTreatmentInformationId() != null) {
            if (recovery.getStaffId() != null) {
                recoveryMapper.updateRecoveryDoctorId(recovery.getTreatmentInformationId(), recovery.getStaffId());
            }
            recoveryMapper.updateRecoveryEndTime(recovery.getTreatmentInformationId());
        }
        return recovery;
    }
}
