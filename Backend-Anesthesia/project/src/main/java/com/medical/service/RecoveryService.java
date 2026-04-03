package com.medical.service;

import com.medical.pojo.Recovery;

public interface RecoveryService {

    /** 入室记录：recovery_room_record */
    Recovery save(Recovery recovery);

    /** 出室评估：recovery_area_room_assessment */
    Recovery saveAssessment(Recovery recovery);
}
