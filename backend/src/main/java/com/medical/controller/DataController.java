package com.medical.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medical.pojo.Data;
import com.medical.pojo.Result;
import com.medical.service.DataService;

@RestController("httpDataController")
@RequestMapping(value = "/data")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    // 接受mac地址和生理参数数据
    @PostMapping("/{deviceId}")
    public Result upload(
            @PathVariable String deviceId,
            @RequestBody Data data) {
        dataService.publish(deviceId, data);
        return Result.success();
    }
}
