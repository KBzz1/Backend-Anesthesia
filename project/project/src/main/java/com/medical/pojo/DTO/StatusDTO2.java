package com.medical.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 患者状态信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO2 {

    // 区域名
    String region;

    /**
     * 患者总数
     */
    private Long totalCount;

    /**
     * 患者信息列表
     */
    private List<PatientInfo> patientList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PatientInfo {  // 暂时只返回患者信息，可拓展
        /**
         * 手术信息ID
         */
        private Long surgeryId;

        /**
         * 患者姓名
         */
        private String patientName;
    }
}

