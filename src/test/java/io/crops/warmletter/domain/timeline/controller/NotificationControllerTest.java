package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
@SpringBootTest
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("GET 알림 구독 성공")
    void get_notificationSub_success() throws Exception {
        // given
        SseEmitter mockEmitter = mock(SseEmitter.class);

        when(notificationService.subscribeNotification()).thenReturn(mockEmitter);

        // SSE 구독 API 호출
        mockMvc.perform(get("/api/notifications/sub")
                        .accept("text/event-stream"))  // accept 헤더에 text/event-stream 추가
                .andExpect(status().isOk());
    }


}