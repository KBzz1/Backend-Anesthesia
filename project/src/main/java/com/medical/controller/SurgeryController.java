package com.medical.controller;

import com.medical.pojo.Patient;
import com.medical.pojo.Result;
import com.medical.service.PatientService;
import com.medical.service.SurgeryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/surgery")
public class SurgeryController {

    @Autowired
    private SurgeryService surgeryService;

    // 暂时用患者基本信息创建手术记录，后续会优化为集合
    @PostMapping
    public Result addRecord(@RequestBody Patient patient) {
        Long surgeryId = surgeryService.addRecord(patient);
        return Result.success(surgeryId);
    }

}
