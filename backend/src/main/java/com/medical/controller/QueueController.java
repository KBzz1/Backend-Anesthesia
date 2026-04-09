package com.medical.controller;

import com.medical.pojo.Result;
import com.medical.pojo.request.AppointmentCheckRequest;
import com.medical.pojo.request.AppointmentRequest;
import com.medical.service.QueueService;
import com.medical.utils.constants.QueueConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @GetMapping("/check/{surgeryId}")
    public Result checkForAppointment(@PathVariable Long surgeryId) {
        String message = queueService.checkForAppointment(surgeryId);
        return switch (message) {
            case QueueConstants.UNEVALUATED, QueueConstants.UNPAID, QueueConstants.BOOKED -> Result.error(message);
            case QueueConstants.BOOK_SUCCESS -> Result.success();
            default -> Result.success(message);
        };
    }

    @PutMapping("/appointment")
    public Result appointment(@RequestBody AppointmentRequest request) {
        Long count = queueService.appointment(request.getSurgeryId(), request.getScheduledTime());
        return Result.success(count);
    }

    @PostMapping("/appointment/check")
    public Result checkAppointmentCount(@RequestBody AppointmentCheckRequest request) {
        Integer count = queueService.checkAppointmentCount(request.getScheduledTime());
        return Result.success(count);
    }

    @PostMapping("/register/{surgeryId}")
    public Result register(@PathVariable Long surgeryId) {
        String message = queueService.register(surgeryId);
        return switch (message) {
            case QueueConstants.UNEVALUATED, QueueConstants.UNBOOKED, QueueConstants.REGISTERED -> Result.error(message);
            case QueueConstants.REGISTER_SUCCESS -> Result.success();
            default -> Result.success(message);
        };
    }

    @PostMapping("/miss/{surgeryId}")
    public Result missedCall(@PathVariable Long surgeryId) {
        queueService.miss(surgeryId);
        return Result.success();
    }
}
