package com.medical.mapper;

import com.medical.pojo.Recovery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecoveryMapper {

    /**
     * 入室记录：插入 recovery_room_record 表。
     */
    void insertRoomRecord(Recovery recovery);

    /**
     * 出室评估：插入 recovery_area_room_assessment 表。
     */
    void insertAssessment(Recovery recovery);

    void updateRecoveryEndTime(@Param("treatmentInformationId") Integer treatmentInformationId);
}
