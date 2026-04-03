package com.medical.utils.enums;


import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 区域枚举
 * 定义系统中所有的区域及其映射关系
 */
@Getter
@AllArgsConstructor
public enum AreaEnum {

    // 区域定义：区域ID, 区域名称, 区域描述
    ASSESSMENT("a", "评估区"),
    WAITING("b1", "等待区"),
    PDA_PLATFORM("b2", "PDA平台"),
    PREPARATION("c", "准备区"),
    TREATMENT("d", "诊疗区"),
    RECOVERY("e", "恢复区"),
    MONITOR("f", "监护仪");

    /**
     * 区域ID（唯一标识）
     */
    private final String areaId;

    /**
     * 区域名称（中文显示名）
     */
    private final String areaName;


    /**
     * 根据区域ID获取区域枚举
     * @param areaId 区域ID
     * @return 区域枚举，如果不存在返回null
     */
    public static AreaEnum getByAreaId(String areaId) {
        if (areaId == null) {
            return null;
        }
        for (AreaEnum area : values()) {
            if (area.getAreaId().equals(areaId)) {
                return area;
            }
        }
        return null;
    }

    /**
     * 根据区域ID获取区域名称
     * @param areaId 区域ID
     * @return 区域名称，如果不存在返回原ID
     */
    public static String getAreaName(String areaId) {
        AreaEnum area = getByAreaId(areaId);
        return area != null ? area.getAreaName() : areaId;
    }

    /**
     * 验证区域ID是否有效
     * @param areaId 区域ID
     * @return true-有效，false-无效
     */
    public static boolean isValidAreaId(String areaId) {
        return getByAreaId(areaId) != null;
    }

    /**
     * 获取所有区域ID列表
     * @return 区域ID列表
     */
    public static List<String> getAllAreaIds() {
        return Arrays.stream(values())
                .map(AreaEnum::getAreaId)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有区域信息（用于前端下拉框等）
     * @return 区域信息列表
     */
    public static List<Map<String, String>> getAllAreaInfo() {
        return Arrays.stream(values())
                .map(area -> Map.of(
                        "areaId", area.getAreaId(),
                        "areaName", area.getAreaName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", areaName, areaId);
    }
}

