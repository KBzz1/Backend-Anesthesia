package com.medical.pojo.DTO;

import lombok.Data;

/**
 * 请求体仅包含诊疗/手术 ID，staffId 由 token 提供。
 */
@Data
public class TokenSignRequestDTO {
    private Long surgeryId;
}
