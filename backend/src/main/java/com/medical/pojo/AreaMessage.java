package com.medical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaMessage {

    private MessageType type;
    private String fromArea;
    private String toArea;
    private Object content;
    private Long timestamp;
    private String messageId;

    public enum MessageType {
        CALL_NUMBER,
        NOTIFICATION,
        SYSTEM_MESSAGE,
        COMMAND,
        UPLOAD,
        ACK
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PatientInfo {
        private Long surgeryId;
        private String patientName;
    }
}
