package com.medical.mapper;

import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Patient;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PatientMapper {

    PatientDTO findById(Integer id);

    Long add(Patient patient);

}
