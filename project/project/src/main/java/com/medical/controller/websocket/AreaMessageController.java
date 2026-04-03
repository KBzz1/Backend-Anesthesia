package com.medical.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.AreaMessage;
import com.medical.utils.AreaConnectionManager;
import com.medical.utils.enums.AreaEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/*
客户端连接: 连接到 /ws 端点
订阅消息: 订阅 /data/area/{areaId} 接收本区域消息
发送消息: 发送到 /data/area/call
*/
@Slf4j
@Controller
@RequiredArgsConstructor
public class AreaMessageController {

    // 依赖注入三个核心组件
    private final SimpMessagingTemplate messagingTemplate;  // 消息发送工具
    private final AreaConnectionManager connectionManager;   // 连接管理器
    // private final AreaMessageService messageService;         // 消息服务

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理区域呼叫消息
     * 客户端发送到：/data/area/call
     */
    @MessageMapping("/area/call")
    public void handleAreaCall(@Payload AreaMessage message,
                               SimpMessageHeaderAccessor headerAccessor) { // 消息头信息
        try {
            /* 补充消息元数据
             * enrichMessage会自动添加：
             * - messageId: "uuid-xxx"
             * - timestamp: "2025-11-23 00:32:00"
             * - status: "SENT"
             */
            enrichMessage(message);

            String fromArea = message.getFromArea();
            String toArea = message.getToArea();

            // ===== 验证消息 =====
            // 格式验证
            if (!validateMessage(message)) {
                log.error("消息验证失败: {}", message);
                sendErrorMessage(fromArea, "消息格式错误");
                return;
            }
            // ID验证
            if (!AreaEnum.isValidAreaId(message.getFromArea())) {
                log.error("无效的源区域ID: {}", fromArea);
                sendErrorMessage(fromArea, "无效的源区域ID: " + fromArea);
                return;
            } else {
                // 区域有效则更新状态
                connectionManager.updateActiveTime(fromArea);
            }
            if (!AreaEnum.isValidAreaId(toArea)) {
                log.error("无效的目标区域ID: {}", toArea);
                sendErrorMessage(fromArea, "无效的目标区域ID: " + toArea);
                return;
            }
            // ===== 记录日志 =====
            log.info("收到消息 - 来源: {}({}), 目标: {}({}), 类型: {}",
                    AreaEnum.getAreaName(fromArea), fromArea,
                    AreaEnum.getAreaName(toArea), toArea,
                    message.getType());
            // 打印消息
            System.out.println(objectMapper.writeValueAsString(message.getContent()));
            // 检查目标区域是否在线
            if (!connectionManager.isAreaOnline(toArea)) {
                log.warn("目标区域 {} 不在线", AreaEnum.getAreaName(toArea));
                sendErrorMessage(fromArea, "目标区域 " + AreaEnum.getAreaName(toArea) + " 不在线");
                return;
            }
            // 转发消息到目标区域
            String destination = "/data/area/" + toArea;
            messagingTemplate.convertAndSend(destination, message);
            log.info("消息已转发: {} -> {}, 类型: {}", fromArea, toArea, message.getType());
            // 发送确认消息给发送方
            sendAckMessage(message);

        } catch (Exception e) {
            log.error("处理消息时发生错误", e);
            sendErrorMessage(message.getFromArea(), "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * REST API：获取在线区域列表
     */
    @GetMapping("/areas/online")
    @ResponseBody
    public Map<String, String> getOnlineAreas() {
        return connectionManager.getAllOnlineAreas();
    }
    /**
     * REST API：广播系统消息
     */
    @PostMapping("/areas/broadcast")
    @ResponseBody
    public void broadcastMessage(@RequestBody AreaMessage message) {
        enrichMessage(message);
        message.setType(AreaMessage.MessageType.SYSTEM_MESSAGE);

        // 广播给所有在线区域
        connectionManager.getAllOnlineAreas().keySet().forEach(areaId -> {
            message.setToArea(areaId);
            messagingTemplate.convertAndSend("/data/area/" + areaId, message);
        });
        log.info("系统消息已广播给 {} 个区域", connectionManager.getAllOnlineAreas().size());
    }

    // ========== 私有辅助方法 ==========

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
        ack.setContent(Map.of(
                "originalMessageId", originalMessage.getMessageId()
        ));
        messagingTemplate.convertAndSend(
                "/data/area/" + originalMessage.getFromArea(), ack);
    }
    private void sendErrorMessage(String toArea, String errorMsg) {
        AreaMessage error = new AreaMessage();
        error.setType(AreaMessage.MessageType.SYSTEM_MESSAGE);
        error.setFromArea("system");
        error.setToArea(toArea);
        error.setMessageId(UUID.randomUUID().toString());
        error.setTimestamp(System.currentTimeMillis());
        error.setContent(Map.of("error", errorMsg));
        messagingTemplate.convertAndSend("/data/area/" + toArea, error);
    }
}


