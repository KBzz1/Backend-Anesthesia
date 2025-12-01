package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindingInfo {
    private Long surgeryId; // treatment_information_id
    private Long bindTime;
}
