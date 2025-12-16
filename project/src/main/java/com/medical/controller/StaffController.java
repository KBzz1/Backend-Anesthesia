package com.medical.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medical.pojo.DTO.StaffInfoDTO;
import com.medical.pojo.Result;
import com.medical.service.MedicalStaffInfoService;

@RestController
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private MedicalStaffInfoService medicalStaffInfoService;

    @GetMapping("/information/{staffId}")
    public Result getStaffInformation(@PathVariable Long staffId) {
        StaffInfoDTO staffInfo = medicalStaffInfoService.getStaffInfo(staffId);
        return Result.success(staffInfo);
    }
}
