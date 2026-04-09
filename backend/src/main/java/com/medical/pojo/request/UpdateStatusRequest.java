package com.medical.pojo.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotBlank(message = "手术ID不能为空")
    private String surgeryId;

    @NotNull(message = "手术ID不能为空")
    @Min(value = 1, message = "状态码必须在1-8之间")
    @Max(value = 8, message = "状态码必须在1-8之间")
    private Integer statusCode;
}
