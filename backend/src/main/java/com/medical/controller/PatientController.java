package com.medical.controller;

import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Result;
import com.medical.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{id}")
    public Result findById(@PathVariable("id") Integer id) {
        PatientDTO patientDTO = patientService.findById(id);
        return Result.success(patientDTO);
    }

}
