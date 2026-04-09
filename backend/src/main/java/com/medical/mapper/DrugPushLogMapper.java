package com.medical.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.medical.pojo.DTO.DrugRecordItemDTO;

@Mapper
public interface DrugPushLogMapper {

    List<DrugRecordItemDTO> selectByTreatmentInformationId(
            @Param("treatmentInformationId") Long treatmentInformationId);
}
