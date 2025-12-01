package com.medical.service;

import java.util.List;

import com.medical.pojo.DTO.WaveformSeriesDTO;

public interface WaveformService {
    List<WaveformSeriesDTO> getWaveformData(Long treatmentInformationId);
}
