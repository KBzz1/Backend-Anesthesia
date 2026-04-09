package com.medical.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medical.pojo.DTO.AnesthesiaRecordSummaryDTO;
import com.medical.pojo.DTO.RecoveryEventRecord;
import com.medical.pojo.Result;
import com.medical.service.ARSService;
import com.medical.service.RERService;

@RestController
@RequestMapping("/ARS")
public class ARSController {

    @Autowired
    private ARSService arsService;

    @Autowired
    private RERService rerService;

    @GetMapping("/getPatients")
    public Result getPatients() {
        return Result.success(arsService.getPatientsToday());
    }

    @GetMapping("/{treatmentId}")
    public Result getSummary(@PathVariable Long treatmentId) {
        AnesthesiaRecordSummaryDTO dto = arsService.getByTreatmentId(treatmentId);
        // 联动拼接 RER 事件数据
        RecoveryEventRecord rer = rerService.getRERByTreatmentId(treatmentId.intValue());
        if (dto != null && rer != null) {
            // 拼接到 data 字段
            dto.setIntraoperative(rer.getIntraoperative());
            dto.setComplication(rer.getComplication());
            dto.setMonitoring(rer.getMonitoring());
        }
        return Result.success(dto);
    }
}
