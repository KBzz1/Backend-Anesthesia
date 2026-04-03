package com.medical.controller;

import com.medical.pojo.DTO.PaaAssessmentRequest;
import com.medical.pojo.PaaInformation;
import com.medical.pojo.Result;
import com.medical.service.PaaInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 术前麻醉评估信息（paa_information）管理
 *
 * 前端 JSON 模板：
 * {
 *   "surgeryId": 1,
 *   "height": 170.5,
 *   "weight": 65.2,
 *   "hisIsHypertension": true,
 *   "hisIsDiabetes": false,
 *   "smokeHis": "无",
 *   "drinkHis": "偶尔",
 *   "chiefComplaint": "术前评估",
 *   "asaClass": "II",
 *   "asaClassSuggestion": "II",
 *   "anesthesiaPlanSuggestion": "全麻"
 *   ... 
 * }
 */
@RestController
@RequestMapping("/paa")
public class PaaInformationController {

    @Autowired
    private PaaInformationService paaInformationService;

    /**
     * 根据手术/治疗信息ID（surgeryId）查询对应的 PAA 记录
     */
    @GetMapping("/byTreatment/{surgeryId}")
    public Result getByTreatment(@PathVariable Long surgeryId) {
        PaaInformation paaInformation = paaInformationService.getBySurgeryId(surgeryId);
        if (paaInformation == null) {
            return Result.error("未找到该手术ID对应的评估记录: " + surgeryId);
        }
        return Result.success(paaInformation);
    }

    @PostMapping
    public Result create(@RequestBody PaaInformation paaInformation) {
        paaInformationService.save(paaInformation);
        return Result.success();
    }

    /**
     * 一次性接收患者、诊疗、PAA 三段数据并落库。
     */
    @PostMapping("/assessment")
    public Result createAssessment(@RequestBody PaaAssessmentRequest request) {
        Long treatmentId = paaInformationService.saveAssessment(request);
        return Result.success(treatmentId);
    }
}
