package com.medical.mapper;

import com.medical.pojo.Patient;
import com.medical.pojo.Surgery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SurgeryMapper {

    Long addRecord(Surgery surgery);

    Surgery getRecord(Long id);

    void updateScheduleTime(Long surgeryId, LocalDateTime appointmentRequestTime, LocalDateTime scheduledSurgeryTime);

    void updateDeviceBindTime(Long surgeryId, LocalDateTime deviceBindTime);

    void updateSurgeryStartTime(Long surgeryId, LocalDateTime surgeryStartTime);

    void updateSurgeryEndTime(Long surgeryId, LocalDateTime surgeryEndTime);

    void updateRecoveryEndTime(Long surgeryId, LocalDateTime recoveryEndTime);

    boolean existsBySurgeryId(@Param("id") Long id);

}
