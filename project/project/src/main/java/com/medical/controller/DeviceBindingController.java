package com.medical.controller;

import com.medical.pojo.DTO.DeviceBindingDTO;
import com.medical.pojo.request.BindDeviceRequest;
import com.medical.pojo.Result;
import com.medical.service.DeviceBindingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/device/binding")
@Slf4j
@Controller
public class DeviceBindingController {

    @Autowired
    private DeviceBindingService deviceBindingService;

    /**
     * 绑定设备与患者（自动处理旧绑定）
     */
    @PostMapping
    public Result bindDevice(@RequestBody BindDeviceRequest request) {
        deviceBindingService.bindDevice(request.getSurgeryId(), request.getMacAddress());
        return Result.success();
    }

    /**
     * 通过设备MAC地址解绑
     */
    @DeleteMapping("/device/{macAddress}")
    public Result unbindDevice(@PathVariable String macAddress) {
        deviceBindingService.unbindDevice(macAddress);
        return Result.success();
    }
    /**
     * 通过患者手术ID解绑
     */
    @DeleteMapping("/patient/{surgeryId}")
    public Result unbindDeviceBySurgeryId(@PathVariable String surgeryId) {
        deviceBindingService.unbindDeviceBySurgeryId(surgeryId);
        return Result.success();
    }

    /**
     * 通过设备MAC地址查询绑定信息
     */
    @GetMapping("/device/{macAddress}")
    public Result getDeviceBindingInfo(@PathVariable String macAddress) {
        DeviceBindingDTO bindingInfo = deviceBindingService.getPatient(macAddress);
        return Result.success(bindingInfo);
    }
    /**
     * 通过患者手术ID查询绑定的设备
     */
    @GetMapping("/patient/{surgeryId}")
    public Result getDeviceBySurgeryId(@PathVariable String surgeryId) {
        String macAddress = deviceBindingService.getDevice(surgeryId);
        return Result.success(macAddress);
    }

    /**
     * 获取所有绑定统计信息
     */
    @GetMapping("/statistics")
    public Result getBindingStatistics() {
        Map<String, String> statistics = deviceBindingService.getBindingStatistics();
        return Result.success(statistics);
    }




}
