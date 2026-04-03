package com.medical.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medical.mapper.WaveformMapper;
import com.medical.pojo.DTO.WaveformDTO;
import com.medical.pojo.DTO.WaveformPointDTO;
import com.medical.pojo.DTO.WaveformSeriesDTO;
import com.medical.service.WaveformService;

@Service
public class WaveformServiceImpl implements WaveformService {

    @Autowired
    private WaveformMapper waveformMapper;

    private static final Logger logger = LoggerFactory.getLogger(WaveformServiceImpl.class);

    @Override
    public List<WaveformSeriesDTO> getWaveformData(Long treatmentInformationId) {
        logger.info("getWaveformData called for treatmentInformationId={}", treatmentInformationId);
        // 1. 获取原始扁平数据
        List<WaveformDTO> rawData = waveformMapper.getWaveformData(treatmentInformationId);
        logger.info("rawData size for treatmentInformationId={}: {}", treatmentInformationId, rawData == null ? 0 : rawData.size());
        if (rawData != null && !rawData.isEmpty()) {
            // 打印一些样本以便排查时区/时间问题（最多各取前后 10 条）
            List<WaveformDTO> head = rawData.stream().limit(10).collect(Collectors.toList());
            List<WaveformDTO> tail = rawData.stream().skip(Math.max(0, rawData.size() - 10)).collect(Collectors.toList());
            logger.info("sample head (up to 10) for treatment {}: {}",
                    treatmentInformationId,
                    head.stream().map(p -> String.format("%s|param=%d|v=%s", p.getTime(), p.getParameterId(), p.getValue())).collect(Collectors.joining(", ")));
            logger.info("sample tail (up to 10) for treatment {}: {}",
                    treatmentInformationId,
                    tail.stream().map(p -> String.format("%s|param=%d|v=%s", p.getTime(), p.getParameterId(), p.getValue())).collect(Collectors.joining(", ")));
        }
        if (rawData == null || rawData.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 按 parameterId 分组
        Map<Integer, List<WaveformDTO>> groupedData = rawData.stream()
                .collect(Collectors.groupingBy(WaveformDTO::getParameterId));

        // 3. 组装为前端期望的 JSON 结构
        List<WaveformSeriesDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<WaveformDTO>> entry : groupedData.entrySet()) {
            Integer parameterId = entry.getKey();
            List<WaveformDTO> points = entry.getValue();

            // 记录每个 parameter 的时间范围，方便确认时区偏移或时间超出预期
            try {
                List<java.time.Instant> times = points.stream().map(WaveformDTO::getTime).collect(Collectors.toList());
                java.time.Instant min = Collections.min(times);
                java.time.Instant max = Collections.max(times);
                logger.info("treatment={}, parameterId={}, points={}, timeRange=[{}, {}]",
                        treatmentInformationId, parameterId, times.size(), min, max);
            } catch (Exception e) {
                logger.warn("failed to compute time range for treatment {} parameter {}: {}", treatmentInformationId, parameterId, e.getMessage());
            }

            List<WaveformPointDTO> pointDTOs = points.stream()
                    .map(p -> new WaveformPointDTO(p.getTime(), p.getValue()))
                    .collect(Collectors.toList());

            result.add(new WaveformSeriesDTO(parameterId, pointDTOs));
        }

        return result;
    }
}
