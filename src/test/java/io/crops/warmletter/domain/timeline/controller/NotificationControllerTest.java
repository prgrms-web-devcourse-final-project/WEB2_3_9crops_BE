package io.crops.warmletter.domain.timeline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import io.crops.warmletter.domain.timeline.service.TimelineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
@SpringBootTest
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("GET 알림 구독 성공")
    void get_notificationSub_success() throws Exception {
        // given
        int memberId = 1;

        SseEmitter mockEmitter = mock(SseEmitter.class);

        when(notificationService.subscribeNotification(memberId)).thenReturn(mockEmitter);

        // SSE 구독 API 호출
        mockMvc.perform(get("/api/notification/sub/{memberId}", memberId)
                        .accept("text/event-stream"))  // accept 헤더에 text/event-stream 추가
                .andExpect(status().isOk());
    }


}