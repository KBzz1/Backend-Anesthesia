package com.medical.controller;

import com.medical.pojo.DTO.DeviceBindingDTO;
import com.medical.pojo.Result;
import com.medical.pojo.request.BindDeviceRequest;
import com.medical.service.DeviceBindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/device/binding")
@RequiredArgsConstructor
public class DeviceBindingController {

    private final DeviceBindingService deviceBindingService;

    @PostMapping
    public Result bindDevice(@RequestBody BindDeviceRequest request) {
        deviceBindingService.bindDevice(request.getSurgeryId(), request.getMacAddress());
        return Result.success();
    }

    @DeleteMapping("/device/{macAddress}")
    public Result unbindDevice(@PathVariable String macAddress) {
        deviceBindingService.unbindDevice(macAddress);
        return Result.success();
    }

    @DeleteMapping("/patient/{surgeryId}")
    public Result unbindDeviceBySurgeryId(@PathVariable String surgeryId) {
        deviceBindingService.unbindDeviceBySurgeryId(surgeryId);
        return Result.success();
    }

    @GetMapping("/device/{macAddress}")
    public Result getDeviceBindingInfo(@PathVariable String macAddress) {
        DeviceBindingDTO bindingInfo = deviceBindingService.getPatient(macAddress);
        return Result.success(bindingInfo);
    }

    @GetMapping("/patient/{surgeryId}")
    public Result getDeviceBySurgeryId(@PathVariable String surgeryId) {
        return Result.success(deviceBindingService.getDevice(surgeryId));
    }

    @GetMapping("/statistics")
    public Result getBindingStatistics() {
        Map<String, String> statistics = deviceBindingService.getBindingStatistics();
        return Result.success(statistics);
    }
}
