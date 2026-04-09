package com.medical.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.AreaMessage;
import com.medical.utils.AreaConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AreaMessageControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SimpMessagingTemplate messagingTemplate;
    private AreaConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        messagingTemplate = mock(SimpMessagingTemplate.class);
        connectionManager = mock(AreaConnectionManager.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AreaMessageController(messagingTemplate, connectionManager))
                .build();
    }

    @Test
    void getOnlineAreasReturnsCurrentMap() throws Exception {
        when(connectionManager.getAllOnlineAreas()).thenReturn(Map.of("d", "session-1"));

        mockMvc.perform(get("/areas/online"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"d\":\"session-1\"}"));
    }

    @Test
    void broadcastWithoutOnlineAreasReturnsOk() throws Exception {
        when(connectionManager.getAllOnlineAreas()).thenReturn(Map.of());

        AreaMessage message = new AreaMessage();
        message.setFromArea("system");
        message.setContent(Map.of("title", "notice"));

        mockMvc.perform(post("/areas/broadcast")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verifyNoInteractions(messagingTemplate);
    }
}
