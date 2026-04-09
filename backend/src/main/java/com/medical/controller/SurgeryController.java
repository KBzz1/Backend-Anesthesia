package com.medical.controller;

import com.medical.pojo.Patient;
import com.medical.pojo.Result;
import com.medical.service.SurgeryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/surgery")
@RequiredArgsConstructor
public class SurgeryController {

    private final SurgeryService surgeryService;

    @PostMapping
    public Result addRecord(@RequestBody Patient patient) {
        Long surgeryId = surgeryService.addRecord(patient);
        return Result.success(surgeryId);
    }

}
