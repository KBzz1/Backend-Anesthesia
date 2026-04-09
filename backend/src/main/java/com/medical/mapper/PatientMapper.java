package com.medical.mapper;

import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Patient;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PatientMapper {

    PatientDTO findById(Integer id);

    Long add(Patient patient);

    List<PatientDTO> findByIds(@Param("ids") List<Long> ids);

}
