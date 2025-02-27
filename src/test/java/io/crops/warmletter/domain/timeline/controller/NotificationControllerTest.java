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
        Long memberId = 1L;

        SseEmitter mockEmitter = mock(SseEmitter.class);

        when(notificationService.subscribeNotification(memberId)).thenReturn(mockEmitter);

        // SSE 구독 API 호출
        mockMvc.perform(get("/api/notifications/sub/{memberId}", memberId)
                        .accept("text/event-stream"))  // accept 헤더에 text/event-stream 추가
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH 알림 읽음 상태 변경 성공 - false에서 true")
    void update_notificationRead_success() throws Exception {
        // given
        Long notificationId = 1L;

        ReadNotificationResponse readNotificationResponse = ReadNotificationResponse.builder()
                .notificationId(notificationId)
                .isRead(true)
                .build();

        when(notificationService.updateNotificationRead(notificationId)).thenReturn(readNotificationResponse);

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationId").value(readNotificationResponse.getNotificationId()))
                .andExpect(jsonPath("$.data.isRead").value(true))
                .andExpect(jsonPath("$.message").value("알림 읽음 처리 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("PATCH 모든 알림 읽음 상태 변경 성공 - false에서 true")
    void update_notificationAllRead_success() throws Exception {
        // given
        Long notificationId1 = 1L;
        Long notificationId2 = 2L;

        ReadNotificationResponse readNotificationResponse1 = ReadNotificationResponse.builder().notificationId(notificationId1).isRead(true).build();
        ReadNotificationResponse readNotificationResponse2 = ReadNotificationResponse.builder().notificationId(notificationId2).isRead(true).build();

        List<ReadNotificationResponse> readNotificationResponse = Arrays.asList(readNotificationResponse1, readNotificationResponse2);

        when(notificationService.updateNotificationAllRead()).thenReturn(readNotificationResponse);

        // when & then
        mockMvc.perform(patch("/api/notifications/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].notificationId").value(readNotificationResponse1.getNotificationId()))
                .andExpect(jsonPath("$.data[0].isRead").value(true))
                .andExpect(jsonPath("$.data[1].notificationId").value(readNotificationResponse2.getNotificationId()))
                .andExpect(jsonPath("$.data[1].isRead").value(true))
                .andExpect(jsonPath("$.message").value("모든 알림 읽음 처리 성공"))
                .andDo(print());
    }
}