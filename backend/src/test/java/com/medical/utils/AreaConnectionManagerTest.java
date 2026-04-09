package com.medical.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaConnectionManagerTest {

    @Test
    void registerAreaReplacesPreviousSessionForSameArea() {
        AreaConnectionManager manager = new AreaConnectionManager();

        manager.registerArea("d", "session-1");
        manager.registerArea("d", "session-2");

        assertEquals("session-2", manager.getSessionId("d"));
        assertNull(manager.getAreaId("session-1"));
        assertEquals("d", manager.getAreaId("session-2"));
    }

    @Test
    void unregisterAreaRemovesOnlineEntry() {
        AreaConnectionManager manager = new AreaConnectionManager();

        manager.registerArea("e", "session-9");
        manager.unregisterArea("session-9");

        assertTrue(manager.getAllOnlineAreas().isEmpty());
        assertNull(manager.getSessionId("e"));
    }
}
