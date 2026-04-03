package com.medical.utils;

/**
 * Legacy MQTT manager placeholder. The project currently ingests monitoring
 * data via STOMP/WebSocket (`StompDataConsumer`), so the MQTT implementation
 * has been removed to simplify dependencies. Keep this empty type to preserve
 * package compatibility for any downstream code that still references it.
 */
public final class MqttManager {
    private MqttManager() {
        // intentionally empty
    }
}

