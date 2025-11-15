package com.medical.mapper;

import com.medical.pojo.Patient;
import com.medical.pojo.Surgery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SurgeryMapper {

    Long addRecord(Surgery surgery);


}
