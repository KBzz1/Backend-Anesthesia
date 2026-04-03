package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindingInfo {
    private String surgeryId; // treatment_information_id (接口返回String)
    private Long bindTime;
}
