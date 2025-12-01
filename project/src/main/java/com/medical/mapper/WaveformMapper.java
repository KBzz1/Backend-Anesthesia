package com.medical.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.medical.pojo.DTO.WaveformDTO;

@Mapper
public interface WaveformMapper {
    List<WaveformDTO> getWaveformData(@Param("treatmentInformationId") Long treatmentInformationId);
}
