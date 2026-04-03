package com.medical.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medical.pojo.DTO.AnesthesiologistRequestDTO;
import com.medical.pojo.DTO.SurgeryAreaDTO;
import com.medical.pojo.DTO.SurgeryAreaRecordDTO;
import com.medical.pojo.DTO.TokenSignRequestDTO;
import com.medical.pojo.Result;
import com.medical.service.SurgeryAreaService;

@RestController
@RequestMapping("/surgeryArea")
public class SurgeryAreaController {

    @Autowired
    private SurgeryAreaService surgeryAreaService;

    @GetMapping("/{surgeryId}")
    public Result getSurgeryAreaInfo(@PathVariable Long surgeryId) {
        SurgeryAreaDTO surgeryAreaDTO = surgeryAreaService.getSurgeryAreaInfo(surgeryId);
        return Result.success(surgeryAreaDTO);
    }

    @PostMapping("/record/{surgeryId}")
    public Result saveSurgeryAreaRecord(@PathVariable Long surgeryId, @RequestBody SurgeryAreaRecordDTO recordDTO) {
        surgeryAreaService.saveSurgeryAreaRecord(surgeryId, recordDTO);
        return Result.success();
    }

    // 旧接口：保持原有行为，body 需要包含 staffId
    @PostMapping("/anesthesiologist")
    public Result saveAnesthesiologist(@RequestBody AnesthesiologistRequestDTO requestDTO) {
        String signature = surgeryAreaService.saveAnesthesiologist(requestDTO);
        Map<String, String> data = new HashMap<>();
        data.put("signature", signature);
        return Result.success(data);
    }

    // 新接口：不从 body 传 staffId，staffId 由 token 提供
    @PostMapping("/anesthesiologist2")
    public Result saveAnesthesiologistWithToken(@RequestAttribute("auth.staffId") Long tokenStaffId,
                                                @RequestBody TokenSignRequestDTO requestDTO) {
        if (tokenStaffId == null) {
            return Result.error("Unauthorized: missing staffId in token");
        }

        AnesthesiologistRequestDTO dto = new AnesthesiologistRequestDTO();
        dto.setSurgeryId(requestDTO.getSurgeryId());
        dto.setStaffId(tokenStaffId);

        String signature = surgeryAreaService.saveAnesthesiologist(dto);
        Map<String, String> data = new HashMap<>();
        data.put("signature", signature);
        return Result.success(data);
    }

    // 仅获取当前登录医生的签名，不需要任何 body 参数
    @PostMapping("/mySignature")
    public Result getMySignature(@RequestAttribute("auth.staffId") Long tokenStaffId) {
        if (tokenStaffId == null) {
            return Result.error("Unauthorized: missing staffId in token");
        }

        String signature = surgeryAreaService.getSignatureOnly(tokenStaffId);
        Map<String, String> data = new HashMap<>();
        data.put("signature", signature);
        return Result.success(data);
    }
}
