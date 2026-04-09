package com.medical.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.medical.pojo.DTO.SurgeryAreaDTO;
import com.medical.pojo.DrugPushLog;
import com.medical.pojo.SurgeryStep;

@Mapper
public interface SurgeryAreaMapper {
    SurgeryAreaDTO getSurgeryAreaInfo(@Param("surgeryId") Long surgeryId);

    void insertDrugPushLogs(@Param("list") List<DrugPushLog> list);

    void insertSurgerySteps(@Param("list") List<SurgeryStep> list);

    void updateAnesthesiologist(@Param("surgeryId") Long surgeryId, @Param("staffId") Long staffId);

    // 返回 List<Map>，避免 MyBatis 对 byte[] 的特殊集合推断
    List<Map<String, Object>> getSignature(@Param("staffId") Long staffId);
}
