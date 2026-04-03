package com.medical.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaMessage {

    // 消息类型
    private MessageType type;
    // 源区域ID
    private String fromArea;
    // 目标区域ID
    private String toArea;
    // 消息内容
    private Object content;
    // 时间戳
    private Long timestamp;
    // 消息ID（每条消息的唯一标识，用UUID生成）
    private String messageId;
    // 消息类型枚举：定义系统支持的所有消息类型
    public enum MessageType {
        CALL_NUMBER,      // 叫号消息：用于呼叫患者
        NOTIFICATION,     // 通知消息：一般性通知
        SYSTEM_MESSAGE,   // 系统消息：系统级别的广播
        COMMAND,          // 下行控制
        UPLOAD,           // 上传数据（监护仪）
        ACK               // 确认消息：确认收到某条消息
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PatientInfo {
        // 手术信息ID
        private Long surgeryId;
        // 患者姓名
        private String patientName;
        // 手术室信息待加
    }
}


