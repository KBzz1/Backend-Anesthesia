package com.medical.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.medical.pojo.MedicalStaffInfo;

@Mapper
public interface MedicalStaffInfoMapper {
    MedicalStaffInfo getStaffById(@Param("staffId") Long staffId);
}
