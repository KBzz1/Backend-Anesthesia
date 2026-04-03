package com.medical.utils.constants;

public final class SuperPatientConstants {

    public static final int SUPER_PATIENT_ID = 0;
    public static final long SUPER_SURGERY_ID = 0L;

    private SuperPatientConstants() {
    }

    public static boolean isSuperPatient(String surgeryId) {
        return surgeryId != null && surgeryId.trim().equals(String.valueOf(SUPER_SURGERY_ID));
    }

    public static boolean isSuperPatient(Long surgeryId) {
        return surgeryId != null && surgeryId == SUPER_SURGERY_ID;
    }
}
