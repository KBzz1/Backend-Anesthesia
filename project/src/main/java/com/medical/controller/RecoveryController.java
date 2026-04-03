package com.medical.controller;

import com.medical.pojo.Recovery;
import com.medical.pojo.Result;
import com.medical.service.RecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recovery")
public class RecoveryController {

    private static final Logger log = LoggerFactory.getLogger(RecoveryController.class);

    @Autowired
    private RecoveryService recoveryService;

    @PostMapping
    public Result create(@RequestAttribute("auth.staffId") Long tokenStaffId,
                         @RequestBody Recovery recovery) {
        recovery.setStaffId(tokenStaffId);
        log.info("[recovery] 反序列化后对象: {}", recovery);
        Recovery saved = recoveryService.save(recovery);
        return Result.success(saved);
    }

    @PostMapping("/out")
    public Result createAssessment(@RequestAttribute("auth.staffId") Long tokenStaffId,
                                   @RequestBody Recovery recovery) {
        recovery.setStaffId(tokenStaffId);
        log.info("[recovery/out] 反序列化后对象: {}", recovery);
        Recovery saved = recoveryService.saveAssessment(recovery);
        return Result.success(saved);
    }
}
