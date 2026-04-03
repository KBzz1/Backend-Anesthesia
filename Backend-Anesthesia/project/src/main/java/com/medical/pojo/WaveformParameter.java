package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaveformParameter {
    private Instant time;
    private Long treatmentInformationId;
    private Integer parameterId;
    private Float value;
}
