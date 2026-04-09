package com.medical.pojo.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaveformSeriesDTO {
    private Integer parameterId;
    private List<WaveformPointDTO> points;
}
