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
            List<DrugRecordItemDTO> drugRecords = drugPushLogMapper.selectByTreatmentInformationId(treatmentId);
            if (drugRecords != null && !drugRecords.isEmpty()) {
                dto.setDrugRecord(drugRecords);
            }

            // 填充麻醉医师信息
            Long anesthesiologistId = arsMapper.findAnesthesiologistIdByTreatmentId(treatmentId);
            if (anesthesiologistId != null) {
                String name = arsMapper.findStaffNameById(anesthesiologistId);
                LOGGER.debug("findSignatureById called with anesthesiologistId={}, class={}", anesthesiologistId, Long.class);
                List<Object> signatureList = arsMapper.findSignatureById(anesthesiologistId);
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
                AnesthesiaRecordSummaryDTO.AnesthesiologistInfo info = new AnesthesiaRecordSummaryDTO.AnesthesiologistInfo();
                info.setName(name);
                if (signatureBytes != null) {
                    String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);
                    info.setSignature("data:image/png;base64," + base64Signature);
                }
                dto.setAnesthesiologist(info);
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
}
