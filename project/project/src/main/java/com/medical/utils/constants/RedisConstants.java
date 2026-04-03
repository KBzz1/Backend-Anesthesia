package com.medical.utils.constants;

public class RedisConstants {

    // 患者状态管理
    public static final String PATIENT_INFO_KEY = "patient:info:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String PATIENT_STATE_KEY = "patient:status:";
    public static final Long LOGIN_USER_TTL = 36000L;

    // ========== 设备绑定相关 ==========
    // Hash - 设备绑定详细信息
    public static final String DEVICE_BINDING_KEY = "device:binding:";
    // String - 患者绑定的设备
    public static final String PATIENT_DEVICE_KEY = "patient:device:";
    // Set - 已绑定设备集合
    public static final String DEVICE_BOUND_SET = "device:bound:set";

    // ========== 预约记录相关 ==========
    public static final String APPOINTMENT_COUNT_PREFIX = "appointment:count:";
    public static final String APPOINTMENT_SLOT_PREFIX = "appointment:slot:";
    public static final String APPOINTMENT_SURGERY_PREFIX = "appointment:surgery:";
    public static final String DEMO_APPOINTMENT_COUNT_PREFIX = "demo:appointment:count:";
    public static final String DEMO_APPOINTMENT_SLOT_PREFIX = "demo:appointment:slot:";
    public static final String DEMO_APPOINTMENT_SURGERY_PREFIX = "demo:appointment:surgery:";

}
