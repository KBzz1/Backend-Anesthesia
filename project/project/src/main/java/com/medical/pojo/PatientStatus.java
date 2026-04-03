package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientStatus {
    /**
     * 状态码
     */
    private Integer statusCode;

    /**
     * 更新时间
     */
    private Long updateTime;
}

