package com.medical.mapper;

import com.medical.pojo.Surgery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SurgeryMapper {

    void addRecord(Surgery surgery);

    Surgery getRecord(Long id);

    void updateScheduleTime(Long surgeryId, LocalDateTime appointmentRequestTime, LocalDateTime scheduledSurgeryTime);

    // 三大时间节点记录
    void updateDeviceBindTime(Long surgeryId, LocalDateTime deviceBindTime);
    void updateSurgeryStartTime(Long surgeryId, LocalDateTime surgeryStartTime);
    void updateSurgeryEndTime(Long surgeryId, LocalDateTime surgeryEndTime);
    void updateRecoveryEndTime(Long surgeryId, LocalDateTime recoveryEndTime);

    // 校验单个 treatment_information_id 是否存在
    boolean existsBySurgeryId(@Param("id") Long id);

}
