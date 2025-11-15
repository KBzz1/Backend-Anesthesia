package com.medical.controller;

import com.medical.pojo.Data;
import com.medical.pojo.Result;
import com.medical.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 监测数据管理
@RestController
@RequestMapping(value = "/data")
public class DataController {

    @Autowired
    private DataService dataService;

    // 接受mac地址和生理参数数据
    @PostMapping("/{deviceId}")
    public Result upload(
            @PathVariable String deviceId,
            @RequestBody Data data) {

        dataService.publish(deviceId, data);

        return Result.success();
    }

    @GetMapping("/{deviceId}/subscribe")
    public Result subscribe(@PathVariable String deviceId) {
//        return dataService.subscribe(deviceId);
        return null;
    }

}
