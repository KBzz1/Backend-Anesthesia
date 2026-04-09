package com.medical.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum AreaEnum {

    ASSESSMENT("a", "评估区"),
    WAITING("b1", "等待区"),
    PDA_PLATFORM("b2", "PDA平台"),
    PREPARATION("c", "准备区"),
    TREATMENT("d", "诊疗区"),
    RECOVERY("e", "恢复区"),
    MONITOR("f", "监护仪");

    private final String areaId;
    private final String areaName;

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

    public static String getAreaName(String areaId) {
        AreaEnum area = getByAreaId(areaId);
        return area != null ? area.getAreaName() : areaId;
    }

    public static boolean isValidAreaId(String areaId) {
        return getByAreaId(areaId) != null;
    }

    public static List<String> getAllAreaIds() {
        return Arrays.stream(values())
                .map(AreaEnum::getAreaId)
                .collect(Collectors.toList());
    }

    public static List<Map<String, String>> getAllAreaInfo() {
        return Arrays.stream(values())
                .map(area -> Map.of(
                        "areaId", area.getAreaId(),
                        "areaName", area.getAreaName()
                ))
                .collect(Collectors.toList());
    }
}
