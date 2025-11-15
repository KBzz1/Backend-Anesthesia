package com.medical.controller;

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
 *   "treatmentInformationId": 1,
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
     * 根据手术/治疗信息ID查询对应的 PAA 记录
     *
     * 约定：前端拿到的 surgeryId == treatmentInformationId，
     * 可以直接作为 path 变量传入。
     */
    @GetMapping("/byTreatment/{treatmentInformationId}")
    public Result getByTreatment(@PathVariable Long treatmentInformationId) {
        PaaInformation paaInformation = paaInformationService.getByTreatmentInformationId(treatmentInformationId);
        return Result.success(paaInformation);
    }

    @PostMapping
    public Result create(@RequestBody PaaInformation paaInformation) {
        paaInformationService.save(paaInformation);
        return Result.success();
    }
}
