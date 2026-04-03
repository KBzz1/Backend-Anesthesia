package com.medical.controller;

import com.medical.pojo.DTO.StatusDTO1;
import com.medical.pojo.DTO.StatusDTO2;
import com.medical.pojo.PatientStatus;
import com.medical.pojo.Result;
import com.medical.pojo.request.UpdateStatusRequest;
import com.medical.service.PatientStatusService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    状态码 -> 状态
    1 -> 已评估
    2 -> 已预约
    3 -> 已签到
    4 -> 已佩戴
    5 -> 进入准备区
    6 -> 已准备（建立静脉通道）
    7 -> 手术中
    8 -> 恢复中
*/
@RestController
@RequestMapping("/patients/status")
@Slf4j
public class PatientStatusController {

    @Autowired
    private PatientStatusService patientStatusService;

    /**
     * 更新患者状态
     */
    @PostMapping
    public Result updateStatus(@RequestBody @Valid UpdateStatusRequest request) {
        patientStatusService.updatePatientStatus(
                request.getSurgeryId(),
                request.getStatusCode(),
                true
        );
        return Result.success();
    }
    /**
     * 查询患者当前状态
     */
    @GetMapping("/{surgeryId}")
    public Result queryStatus(@PathVariable String surgeryId) {
        PatientStatus status = patientStatusService.getPatientStatus(surgeryId);
        return Result.success(status);
    }


    // 获取指定状态的患者列表
    @GetMapping("/statusCode/{statusCode}")
    public Result queryPatients(@PathVariable Integer statusCode) {
        StatusDTO1 statusDTO1 = patientStatusService.getPatientInfoByStatus(statusCode);
        return Result.success(statusDTO1);
    }

    // 获取所有区域信息
    @GetMapping("/statistics")
    public Result queryAllRegion() {
        List<StatusDTO2> statusDTO2 = patientStatusService.getAllRegionStatistics();
        return Result.success(statusDTO2);
    }


    @DeleteMapping("/{surgeryId}")
    public Result deleteInfo(@PathVariable String surgeryId) {
        patientStatusService.deletePatientStatus(surgeryId);
        return Result.success();
    }



}
