package com.medical.utils.enums;

import lombok.Getter;

@Getter
public enum PatientStatusEnum {
    EVALUATED(1, "已评估"),
    RESERVED(2, "已预约"),
    CHECKED_IN(3, "已签到"),
    EQUIPPED(4, "已佩戴"),
    ENTER_PRE(5, "进入准备区"),
    PREPARING(6, "已准备（建立静脉通道）"),
    IN_SURGERY(7, "手术中"),
    RECOVERING(8, "恢复中");

    private final int code;
    private final String desc;

    PatientStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}

