package com.medical.service.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medical.mapper.SurgeryAreaMapper;
import com.medical.pojo.DTO.AnesthesiologistRequestDTO;
import com.medical.pojo.DTO.SurgeryAreaDTO;
import com.medical.pojo.DTO.SurgeryAreaRecordDTO;
import com.medical.pojo.DrugPushLog;
import com.medical.pojo.SurgeryStep;
import com.medical.service.SurgeryAreaService;

@Service
public class SurgeryAreaServiceImpl implements SurgeryAreaService {

    private static final Logger log = LoggerFactory.getLogger(SurgeryAreaServiceImpl.class);

    @Autowired
    private SurgeryAreaMapper surgeryAreaMapper;

    @Override
    public SurgeryAreaDTO getSurgeryAreaInfo(Long surgeryId) {
        return surgeryAreaMapper.getSurgeryAreaInfo(surgeryId);
    }

    @Override
    @Transactional
    public void saveSurgeryAreaRecord(Long surgeryId, SurgeryAreaRecordDTO recordDTO) {
        if (recordDTO.getDrugRecord() != null && !recordDTO.getDrugRecord().isEmpty()) {
            List<DrugPushLog> drugPushLogs = new ArrayList<>();
            for (SurgeryAreaRecordDTO.DrugRecordItem item : recordDTO.getDrugRecord()) {
                DrugPushLog drugPushLog = new DrugPushLog();
                drugPushLog.setDrugName(item.getDrugName());
                drugPushLog.setPushTime(item.getPushTime());
                drugPushLog.setDosage(item.getDosage());
                drugPushLog.setUnit(item.getUnit());
                drugPushLog.setTreatmentInformationId(surgeryId);
                drugPushLogs.add(drugPushLog);
            }
            surgeryAreaMapper.insertDrugPushLogs(drugPushLogs);
        }

        if (recordDTO.getSurgeryRecord() != null && !recordDTO.getSurgeryRecord().isEmpty()) {
            List<SurgeryStep> surgerySteps = new ArrayList<>();
            for (SurgeryAreaRecordDTO.SurgeryRecordItem item : recordDTO.getSurgeryRecord()) {
                SurgeryStep step = new SurgeryStep();
                step.setStepName(item.getEventName());
                step.setStepTime(item.getEventTime());
                step.setTreatmentInformationId(surgeryId);
                surgerySteps.add(step);
            }
            surgeryAreaMapper.insertSurgerySteps(surgerySteps);
        }
    }

@Override
public String saveAnesthesiologist(AnesthesiologistRequestDTO requestDTO) {
    log.info("[saveAnesthesiologist] surgeryId={}, staffId={}",
            requestDTO.getSurgeryId(), requestDTO.getStaffId());

    // 调用前，打印 Mapper 代理与其返回类型信息
    try {
        Object mapperProxy = surgeryAreaMapper;
        log.info("[saveAnesthesiologist] mapper proxy class={}", mapperProxy.getClass().getName());

        java.lang.reflect.InvocationHandler handler =
                java.lang.reflect.Proxy.getInvocationHandler(mapperProxy);
        log.info("[saveAnesthesiologist] mapper invocation handler class={}", handler.getClass().getName());

        // 通过反射拿到 getSignature 方法在接口上的声明返回类型
        java.lang.reflect.Method m = com.medical.mapper.SurgeryAreaMapper.class
                .getMethod("getSignature", Long.class);
        log.info("[saveAnesthesiologist] interface getSignature returnType={}", m.getReturnType());

    } catch (Throwable e) {
        log.warn("[saveAnesthesiologist] failed to introspect mapper proxy", e);
    }

    surgeryAreaMapper.updateAnesthesiologist(requestDTO.getSurgeryId(), requestDTO.getStaffId());

    List<Map<String, Object>> rows = surgeryAreaMapper.getSignature(requestDTO.getStaffId());
    if (rows == null || rows.isEmpty()) {
        log.info("[saveAnesthesiologist] getSignature returned empty result");
        return null;
    }

    Map<String, Object> firstRow = rows.get(0);
    if (firstRow == null) {
        log.info("[saveAnesthesiologist] first row is null (signature column might be NULL in DB)");
        return null;
    }

    Object raw = firstRow.get("signature");
    if (raw == null) {
        log.info("[saveAnesthesiologist] signature column is null");
        return null;
    }

    log.info("[saveAnesthesiologist] raw signature value class={}, toString={}",
            raw.getClass().getName(), String.valueOf(raw));

    byte[] signatureBytes;
    if (raw instanceof byte[] bytes) {
        signatureBytes = bytes;
    } else if (raw instanceof java.sql.Blob blob) {
        try {
            signatureBytes = blob.getBytes(1, (int) blob.length());
        } catch (Exception e) {
            log.warn("[saveAnesthesiologist] failed to read Blob signature", e);
            return null;
        }
	    } else {
	        log.warn("[saveAnesthesiologist] unexpected signature Java type: {}", raw.getClass().getName());
	        return null;
	    }
	
	    log.info("[saveAnesthesiologist] final signatureBytes length={}", signatureBytes.length);
	    byte[] normalizedSignatureBytes = normalizeSignatureBytes(signatureBytes, "saveAnesthesiologist");
	    String mimeType = detectMimeType(normalizedSignatureBytes, "saveAnesthesiologist");
	    String base64Signature = Base64.getEncoder().encodeToString(normalizedSignatureBytes);
	    return "data:" + mimeType + ";base64," + base64Signature;
	    }

    @Override
    public String getSignatureOnly(Long staffId) {
        log.info("[getSignatureOnly] staffId={}", staffId);
        
        List<Map<String, Object>> rows = surgeryAreaMapper.getSignature(staffId);
        if (rows == null || rows.isEmpty()) {
            log.info("[getSignatureOnly] getSignature returned empty result");
            return null;
        }

        Map<String, Object> firstRow = rows.get(0);
        if (firstRow == null) {
            log.info("[getSignatureOnly] first row is null");
            return null;
        }

        Object raw = firstRow.get("signature");
        if (raw == null) {
            log.info("[getSignatureOnly] signature column is null");
            return null;
        }

        log.info("[getSignatureOnly] raw signature value class={}", raw.getClass().getName());

        byte[] signatureBytes;
        if (raw instanceof byte[] bytes) {
            signatureBytes = bytes;
        } else if (raw instanceof java.sql.Blob blob) {
            try {
                signatureBytes = blob.getBytes(1, (int) blob.length());
            } catch (Exception e) {
                log.warn("[getSignatureOnly] failed to read Blob signature", e);
                return null;
            }
        } else {
            log.warn("[getSignatureOnly] unexpected signature Java type: {}", raw.getClass().getName());
            return null;
        }

        log.info("[getSignatureOnly] final signatureBytes length={}", signatureBytes.length);
        
        // Check if the bytes are actually a hex string (starts with "\x" or "\\x")
        // This happens when the data was stored as hex text instead of actual binary
	        signatureBytes = normalizeSignatureBytes(signatureBytes, "getSignatureOnly");
	        
	        // Detect image format from magic bytes
	        String mimeType = detectMimeType(signatureBytes, "getSignatureOnly");
	        
	        String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);
	        log.info("[getSignatureOnly] Returning base64 length={}, mimeType={}", base64Signature.length(), mimeType);
	        return "data:" + mimeType + ";base64," + base64Signature;
    }

    private byte[] normalizeSignatureBytes(byte[] signatureBytes, String context) {
        if (signatureBytes == null || signatureBytes.length <= 2) {
            return signatureBytes;
        }

        String preview = new String(signatureBytes, 0, Math.min(10, signatureBytes.length), java.nio.charset.StandardCharsets.UTF_8);
        log.info("[{}] data preview: {}", context, preview);

        if (!preview.startsWith("\\x") && !preview.startsWith("\\\\x")) {
            return signatureBytes;
        }

        log.info("[{}] Detected hex-encoded string, converting to binary", context);
        String hexString = new String(signatureBytes, java.nio.charset.StandardCharsets.UTF_8)
                .replace("\\x", "")
                .replace("\\\\x", "");
        try {
            byte[] decoded = hexStringToBytes(hexString);
            log.info("[{}] After hex decode, length={}", context, decoded.length);
            return decoded;
        } catch (Exception e) {
            log.warn("[{}] Failed to decode hex string", context, e);
            return signatureBytes;
        }
    }

    private String detectMimeType(byte[] signatureBytes, String context) {
        String mimeType = "image/png";
        if (signatureBytes == null || signatureBytes.length < 2) {
            return mimeType;
        }

        if ((signatureBytes[0] & 0xFF) == 0xFF && (signatureBytes[1] & 0xFF) == 0xD8) {
            mimeType = "image/jpeg";
            log.info("[{}] Detected JPEG format", context);
        } else if (signatureBytes.length >= 4
                && (signatureBytes[0] & 0xFF) == 0x89
                && (signatureBytes[1] & 0xFF) == 0x50
                && (signatureBytes[2] & 0xFF) == 0x4E
                && (signatureBytes[3] & 0xFF) == 0x47) {
            mimeType = "image/png";
            log.info("[{}] Detected PNG format", context);
        } else {
            log.info("[{}] Unknown image format, first 4 bytes: {:02x} {:02x} {:02x} {:02x}",
                context,
                signatureBytes[0] & 0xFF,
                signatureBytes[1] & 0xFF,
                signatureBytes.length > 2 ? signatureBytes[2] & 0xFF : 0,
                signatureBytes.length > 3 ? signatureBytes[3] & 0xFF : 0);
        }

        return mimeType;
    }
    
    private byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
