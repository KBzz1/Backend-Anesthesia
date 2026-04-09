package com.medical.utils.constants;

public final class PatientFlowRedisKeys {
    private PatientFlowRedisKeys() {
    }

    public static final String PATIENT_INFO_KEY = "patient:info:";
    public static final String PATIENT_STATE_KEY = "patient:status:";
    public static final String APPOINTMENT_COUNT_PREFIX = "appointment:count:";
    public static final String APPOINTMENT_SLOT_PREFIX = "appointment:slot:";
    public static final String APPOINTMENT_SURGERY_PREFIX = "appointment:surgery:";
    public static final String DEMO_APPOINTMENT_COUNT_PREFIX = "demo:appointment:count:";
    public static final String DEMO_APPOINTMENT_SLOT_PREFIX = "demo:appointment:slot:";
    public static final String DEMO_APPOINTMENT_SURGERY_PREFIX = "demo:appointment:surgery:";
}
