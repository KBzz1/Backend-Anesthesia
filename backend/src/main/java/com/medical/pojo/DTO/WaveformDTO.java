package com.medical.pojo.DTO;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaveformDTO {
    private Instant time;
    private Integer parameterId;
    private Double value;
}
