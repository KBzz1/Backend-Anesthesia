package com.medical.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medical.pojo.DTO.AnesthesiologistRequestDTO;
import com.medical.pojo.DTO.SurgeryAreaDTO;
import com.medical.pojo.DTO.SurgeryAreaRecordDTO;
import com.medical.pojo.Result;
import com.medical.service.SurgeryAreaService;

@RestController
@RequestMapping("/surgeryArea")
public class SurgeryAreaController {

    @Autowired
    private SurgeryAreaService surgeryAreaService;

    @GetMapping("/{surgeryId}")
    public Result getSurgeryAreaInfo(@PathVariable Long surgeryId) {
        SurgeryAreaDTO surgeryAreaDTO = surgeryAreaService.getSurgeryAreaInfo(surgeryId);
        return Result.success(surgeryAreaDTO);
    }

    @PostMapping("/record/{surgeryId}")
    public Result saveSurgeryAreaRecord(@PathVariable Long surgeryId, @RequestBody SurgeryAreaRecordDTO recordDTO) {
        surgeryAreaService.saveSurgeryAreaRecord(surgeryId, recordDTO);
        return Result.success();
    }

    @PostMapping("/anesthesiologist")
    public Result saveAnesthesiologist(@RequestBody AnesthesiologistRequestDTO requestDTO) {
        String signature = surgeryAreaService.saveAnesthesiologist(requestDTO);
        Map<String, String> data = new HashMap<>();
        data.put("signature", signature);
        return Result.success(data);
    }
}
