package com.medical.controller;

import com.medical.pojo.DTO.PatientDTO;
import com.medical.pojo.Patient;
import com.medical.pojo.Result;
import com.medical.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/{id}")
    public Result findById(@PathVariable("id") Integer id) {
        PatientDTO patientDTO = patientService.findById(id);
        return Result.success(patientDTO);
    }

}
