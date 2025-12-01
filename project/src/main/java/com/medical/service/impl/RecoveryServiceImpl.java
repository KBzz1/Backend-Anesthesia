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
        return recovery;
    }

    @Override
    public Recovery saveAssessment(Recovery recovery) {
        // 出室评估：插入 recovery_area_room_assessment 表
        recoveryMapper.insertAssessment(recovery);
        return recovery;
    }
}
