package com.medical.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medical.pojo.DTO.WaveformSeriesDTO;
import com.medical.pojo.Result;
import com.medical.service.WaveformService;

@RestController
@RequestMapping("/waveform")
public class WaveformController {

    @Autowired
    private WaveformService waveformService;

    private static final Logger logger = LoggerFactory.getLogger(WaveformController.class);

    @GetMapping("/{treatmentInformationId}")
    public Result getWaveformData(@PathVariable Long treatmentInformationId) {
        logger.info("/waveform/{} called", treatmentInformationId);
        List<WaveformSeriesDTO> data = waveformService.getWaveformData(treatmentInformationId);
        logger.info("/waveform/{} returned {} series", treatmentInformationId, data == null ? 0 : data.size());
        return Result.success(data);
    }
}
