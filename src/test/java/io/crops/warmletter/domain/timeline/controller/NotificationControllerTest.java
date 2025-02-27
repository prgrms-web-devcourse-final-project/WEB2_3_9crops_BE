package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
        int memberId = 1;

        SseEmitter mockEmitter = mock(SseEmitter.class);

        when(notificationService.subscribeNotification(memberId)).thenReturn(mockEmitter);

        // SSE 구독 API 호출
        mockMvc.perform(get("/api/notification/sub/{memberId}", memberId)
                        .accept("text/event-stream"))  // accept 헤더에 text/event-stream 추가
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH 알림 읽음 상태 변경 성공 - false에서 true")
    void update_notificationRead_success() throws Exception {
        // given
        long notificationId = 1L;

        ReadNotificationResponse readNotificationResponse = ReadNotificationResponse.builder()
                .notificationId(notificationId)
                .isRead(true)
                .build();

        when(notificationService.updateNotificationRead(notificationId)).thenReturn(readNotificationResponse);

        // when & then
        mockMvc.perform(patch("/api/notification/{notificationId}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationId").value(readNotificationResponse.getNotificationId()))
                .andExpect(jsonPath("$.data.isRead").value(true))
                .andExpect(jsonPath("$.message").value("알림 읽음 처리 성공"))
                .andDo(print());
    }
}