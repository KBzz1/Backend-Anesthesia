package com.medical.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum PatientRegionEnum {

    ASSESSMENT("评估区", List.of(1)),
    WAITING("等待区", Arrays.asList(3, 4)),
    PREPARATION("准备区", Arrays.asList(5, 6)),
    TREATMENT("诊疗区", List.of(7)),
    RECOVERY("恢复区", List.of(8));

    /**
     * 区域名称
     */
    private final String regionName;

    /**
     * 该区域包含的状态码列表
     */
    private final List<Integer> statusCodes;
}

