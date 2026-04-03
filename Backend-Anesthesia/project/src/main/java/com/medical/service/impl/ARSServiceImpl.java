package com.medical.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medical.mapper.ARSMapper;
import com.medical.mapper.DrugPushLogMapper;
import com.medical.pojo.DTO.AnesthesiaRecordSummaryDTO;
import com.medical.pojo.DTO.DrugRecordItemDTO;
import com.medical.pojo.DTO.PatientSummaryDTO;
import com.medical.pojo.SurgeryStep;
import com.medical.service.ARSService;

@Service
public class ARSServiceImpl implements ARSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ARSServiceImpl.class);

    @Autowired
    private ARSMapper arsMapper;

    @Autowired
    private DrugPushLogMapper drugPushLogMapper;

    @Override
    public List<PatientSummaryDTO> getPatientsToday() {
        return arsMapper.findPatientsByDate(LocalDate.now());
    }

    @Override
    public AnesthesiaRecordSummaryDTO getByTreatmentId(Long treatmentId) {
        AnesthesiaRecordSummaryDTO dto = arsMapper.findByTreatmentId(treatmentId);
        if (dto != null) {
            dto.setIdCardNumber(buildPseudoRegistrationNumber(dto.getPatientId()));

            List<DrugRecordItemDTO> drugRecords = drugPushLogMapper.selectByTreatmentInformationId(treatmentId);
            if (drugRecords != null && !drugRecords.isEmpty()) {
                dto.setDrugRecord(drugRecords);
            }

            // 填充麻醉医师信息
            Long anesthesiologistId = arsMapper.findAnesthesiologistIdByTreatmentId(treatmentId);
            if (anesthesiologistId != null) {
                dto.setAnesthesiologist(buildDoctorInfo(anesthesiologistId));
            }

            Long recoveryDoctorId = arsMapper.findRecoveryDoctorIdByTreatmentId(treatmentId);
            if (recoveryDoctorId != null) {
                dto.setRecoveryDoctor(buildDoctorInfo(recoveryDoctorId));
            }

            List<SurgeryStep> steps = arsMapper.findSurgeryStepsByTreatmentId(treatmentId);
            if (steps != null && !steps.isEmpty()) {
                List<AnesthesiaRecordSummaryDTO.SurgeryRecordItem> recordItems = new ArrayList<>();
                for (SurgeryStep step : steps) {
                    AnesthesiaRecordSummaryDTO.SurgeryRecordItem item = new AnesthesiaRecordSummaryDTO.SurgeryRecordItem();
                    item.setEventName(step.getStepName());
                    if (step.getStepTime() != null) {
                        java.time.LocalDateTime localDateTime = step.getStepTime().toLocalDateTime();
                        item.setEventHour(localDateTime.getHour());
                        item.setEventMinute(localDateTime.getMinute());
                    }
                    recordItems.add(item);
                }
                dto.setSurgeryRecord(recordItems);
            }
        }
        return dto;
    }

    private AnesthesiaRecordSummaryDTO.DoctorInfo buildDoctorInfo(Long staffId) {
        String name = arsMapper.findStaffNameById(staffId);
        LOGGER.debug("findSignatureById called with staffId={}, class={}", staffId, Long.class);
        List<Object> signatureList = arsMapper.findSignatureById(staffId);
        byte[] signatureBytes = null;
        if (signatureList != null && !signatureList.isEmpty()) {
            Object raw = signatureList.get(0);
            if (raw != null) {
                if (raw instanceof byte[]) {
                    signatureBytes = (byte[]) raw;
                } else if (raw instanceof Byte[]) {
                    Byte[] boxed = (Byte[]) raw;
                    signatureBytes = new byte[boxed.length];
                    for (int i = 0; i < boxed.length; i++) {
                        signatureBytes[i] = boxed[i];
                    }
                } else {
                    LOGGER.error("Unexpected signature type: {}", raw.getClass());
                }
            }
        }
        LOGGER.debug("findSignatureById returned {} bytes", signatureBytes == null ? 0 : signatureBytes.length);

        AnesthesiaRecordSummaryDTO.DoctorInfo info = new AnesthesiaRecordSummaryDTO.DoctorInfo();
        info.setName(name);
        if (signatureBytes != null) {
            byte[] normalizedSignatureBytes = normalizeSignatureBytes(signatureBytes);
            String mimeType = detectMimeType(normalizedSignatureBytes);
            String base64Signature = Base64.getEncoder().encodeToString(normalizedSignatureBytes);
            info.setSignature("data:" + mimeType + ";base64," + base64Signature);
        }
        return info;
    }

    private byte[] normalizeSignatureBytes(byte[] signatureBytes) {
        if (signatureBytes == null || signatureBytes.length <= 2) {
            return signatureBytes;
        }

        String preview = new String(signatureBytes, 0, Math.min(10, signatureBytes.length), java.nio.charset.StandardCharsets.UTF_8);
        if (!preview.startsWith("\\x") && !preview.startsWith("\\\\x")) {
            return signatureBytes;
        }

        String hexString = new String(signatureBytes, java.nio.charset.StandardCharsets.UTF_8)
                .replace("\\x", "")
                .replace("\\\\x", "");
        return hexStringToBytes(hexString);
    }

    private String detectMimeType(byte[] signatureBytes) {
        if (signatureBytes == null || signatureBytes.length < 2) {
            return "image/png";
        }

        if ((signatureBytes[0] & 0xFF) == 0xFF && (signatureBytes[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }

        if (signatureBytes.length >= 4
                && (signatureBytes[0] & 0xFF) == 0x89
                && (signatureBytes[1] & 0xFF) == 0x50
                && (signatureBytes[2] & 0xFF) == 0x4E
                && (signatureBytes[3] & 0xFF) == 0x47) {
            return "image/png";
        }

        return "image/png";
    }

    private byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private String buildPseudoRegistrationNumber(Long patientId) {
        if (patientId == null) {
            return "300000000000";
        }
        long normalizedId = Math.floorMod(patientId, 10000000000L);
        return String.format("30%010d", normalizedId);
    }
}
