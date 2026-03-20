package com.medical.controller;

import com.medical.pojo.Result;
import com.medical.pojo.DTO.RecoveryEventRecord;
import com.medical.service.RERService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rer")
public class RERController {

    @Autowired
    private RERService rerService;

    @PostMapping
    public Result saveRER(@RequestBody RecoveryEventRecord record) {
        rerService.saveRER(record);
        return Result.success();
    }
}
