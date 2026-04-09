package com.medical.controller.websocket;

import com.medical.pojo.AreaMessage;
import com.medical.utils.AreaConnectionManager;
import com.medical.utils.enums.AreaEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AreaMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final AreaConnectionManager connectionManager;

    @MessageMapping("/area/call")
    public void handleAreaCall(@Payload AreaMessage message) {
        try {
            enrichMessage(message);

            String fromArea = message.getFromArea();
            String toArea = message.getToArea();

            if (!validateMessage(message)) {
                sendErrorMessage(fromArea, "消息格式错误");
                return;
            }
            if (!AreaEnum.isValidAreaId(fromArea)) {
                sendErrorMessage(fromArea, "无效的源区域ID: " + fromArea);
                return;
            }

            connectionManager.updateActiveTime(fromArea);

            if (!AreaEnum.isValidAreaId(toArea)) {
                sendErrorMessage(fromArea, "无效的目标区域ID: " + toArea);
                return;
            }
            if (!connectionManager.isAreaOnline(toArea)) {
                sendErrorMessage(fromArea, "目标区域 " + AreaEnum.getAreaName(toArea) + " 不在线");
                return;
            }

            messagingTemplate.convertAndSend("/data/area/" + toArea, message);
            sendAckMessage(message);
        } catch (Exception e) {
            log.error("处理区域消息失败", e);
            String fromArea = message != null ? message.getFromArea() : null;
            sendErrorMessage(fromArea, "消息处理失败: " + e.getMessage());
        }
    }

    @GetMapping("/areas/online")
    @ResponseBody
    public Map<String, String> getOnlineAreas() {
        return connectionManager.getAllOnlineAreas();
    }

    @PostMapping("/areas/broadcast")
    @ResponseBody
    public void broadcastMessage(@RequestBody AreaMessage message) {
        enrichMessage(message);
        message.setType(AreaMessage.MessageType.SYSTEM_MESSAGE);
        connectionManager.getAllOnlineAreas().keySet().forEach(areaId -> {
            AreaMessage outbound = copyForRecipient(message, areaId);
            messagingTemplate.convertAndSend("/data/area/" + areaId, outbound);
        });
    }

    private void enrichMessage(AreaMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis());
        }
    }

    private boolean validateMessage(AreaMessage message) {
        return message.getType() != null
                && message.getFromArea() != null
                && message.getToArea() != null
                && message.getContent() != null;
    }

    private void sendAckMessage(AreaMessage originalMessage) {
        AreaMessage ack = new AreaMessage();
        ack.setType(AreaMessage.MessageType.ACK);
        ack.setFromArea("system");
        ack.setToArea(originalMessage.getFromArea());
        ack.setMessageId(UUID.randomUUID().toString());
        ack.setTimestamp(System.currentTimeMillis());
        ack.setContent(Map.of("originalMessageId", originalMessage.getMessageId()));
        messagingTemplate.convertAndSend("/data/area/" + originalMessage.getFromArea(), ack);
    }

    private void sendErrorMessage(String toArea, String errorMsg) {
        if (toArea == null || toArea.isBlank()) {
            log.warn("无法发送区域错误消息，目标区域为空: {}", errorMsg);
            return;
        }
        AreaMessage error = new AreaMessage();
        error.setType(AreaMessage.MessageType.SYSTEM_MESSAGE);
        error.setFromArea("system");
        error.setToArea(toArea);
        error.setMessageId(UUID.randomUUID().toString());
        error.setTimestamp(System.currentTimeMillis());
        error.setContent(Map.of("error", errorMsg));
        messagingTemplate.convertAndSend("/data/area/" + toArea, error);
    }

    private AreaMessage copyForRecipient(AreaMessage message, String areaId) {
        AreaMessage outbound = new AreaMessage();
        outbound.setType(message.getType());
        outbound.setFromArea(message.getFromArea());
        outbound.setToArea(areaId);
        outbound.setContent(message.getContent());
        outbound.setTimestamp(message.getTimestamp());
        outbound.setMessageId(message.getMessageId());
        return outbound;
    }
}
