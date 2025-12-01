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

    Object raw = rows.get(0).get("signature");
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
    String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);
    return "data:image/png;base64," + base64Signature;
    }
}
