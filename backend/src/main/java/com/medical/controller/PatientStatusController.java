package com.medical.controller;

import com.medical.pojo.DTO.StatusDTO1;
import com.medical.pojo.DTO.StatusDTO2;
import com.medical.pojo.PatientStatus;
import com.medical.pojo.Result;
import com.medical.pojo.request.UpdateStatusRequest;
import com.medical.service.PatientStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/patients/status")
@RequiredArgsConstructor
public class PatientStatusController {

    private final PatientStatusService patientStatusService;

    @PostMapping
    public Result updateStatus(@RequestBody @Valid UpdateStatusRequest request) {
        patientStatusService.updatePatientStatus(request.getSurgeryId(), request.getStatusCode(), true);
        return Result.success();
    }

    @GetMapping("/{surgeryId}")
    public Result queryStatus(@PathVariable String surgeryId) {
        PatientStatus status = patientStatusService.getPatientStatus(surgeryId);
        return Result.success(status);
    }

    @GetMapping("/statusCode/{statusCode}")
    public Result queryPatients(@PathVariable Integer statusCode) {
        StatusDTO1 statusDTO1 = patientStatusService.getPatientInfoByStatus(statusCode);
        return Result.success(statusDTO1);
    }

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
