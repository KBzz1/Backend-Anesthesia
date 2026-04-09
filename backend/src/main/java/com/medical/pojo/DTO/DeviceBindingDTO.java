package com.medical.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceBindingDTO {
    private String surgeryId;
    private Long bindTime;
}
