package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Surgery {

    private Long surgeryId;
    private Long patientId;
    private Boolean isEmergency;
    // 记录默认false
    private Boolean isPaid;
    private String surgeryMethod;


//    // 预约请求时间
//    private LocalDateTime appointmentRequestTime;
//    // 安排时间
//    private LocalDateTime scheduledSurgeryTime;
//    // 手术时间
//    private LocalDateTime surgeryDate;
//    // 三个时间节点
//    private LocalDateTime surgeryStartTime;
//    private LocalDateTime surgeryEndTime;
//    private LocalDateTime recoveryEndTime;


}

