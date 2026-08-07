package com.medical.utils.constants;

import com.medical.service.PatientStatusService;

/**
 * SuperPatient 特殊业务策略。
 * <p>
 * SuperPatient (surgeryId=0) 是数据库只读、Redis 缓存可变的演示患者：
 * - 数据库内容只读，不允许写
 * - Redis 缓存状态可以变更
 */
public final class SuperPatientPolicy {

    private SuperPatientPolicy() {
    }

    /**
     * SuperPatient 签到：更新 Redis 缓存状态为 CHECKED_IN，不写数据库。
     *
     * @return Integer 新状态码（已被更新）；null 表示未处理（普通患者）
     */
    public static Integer registerAsSuperPatient(
            Long surgeryId,
            int currentStatusCode,
            PatientStatusService patientStatusService
    ) {
        if (!SuperPatientConstants.isSuperPatient(surgeryId)) {
            return null;
        }
        if (currentStatusCode >= StatusConstants.CHECKED_IN && currentStatusCode != StatusConstants.MISSED) {
            return currentStatusCode;
        }
        patientStatusService.updatePatientStatus(
                String.valueOf(surgeryId),
                StatusConstants.CHECKED_IN,
                true
        );
        return StatusConstants.CHECKED_IN;
    }
}
