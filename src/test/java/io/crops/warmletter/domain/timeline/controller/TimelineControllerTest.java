package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.service.TimelineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
@SpringBootTest
class TimelineControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimelineService timelineService;
    
    @Test
    @DisplayName("GET 타임라인 조회 성공")
    void get_timelines_success() throws Exception {
        // given
        TimelineResponse timeline1 = TimelineResponse.builder().timelineId(1L).title("1111번 편지").alarmType(AlarmType.LETTER).isRead(false).build();
        TimelineResponse timeline2 = TimelineResponse.builder().timelineId(2L).title("1111번 공유 요청").alarmType(AlarmType.SHARE).isRead(false).build();
        List<TimelineResponse> timelines = List.of(timeline2, timeline1);

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TimelineResponse> timelinePage = new PageImpl<>(timelines, pageable, timelines.size());

        when(timelineService.getTimelines(any(Pageable.class))).thenReturn(timelinePage);

        // when & then
        mockMvc.perform(get("/api/timelines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].timelineId").value(timeline2.getTimelineId()))
                .andExpect(jsonPath("$.data.content[0].title").value(timeline2.getTitle()))
                .andExpect(jsonPath("$.data.content[0].alarmType").value(timeline2.getAlarmType().toString()))
                .andExpect(jsonPath("$.data.content[0].read").value(timeline2.isRead()))
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.message").value("타임라인 조회 성공"))
                .andDo(print());
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

        when(timelineService.updateNotificationRead(notificationId)).thenReturn(readNotificationResponse);

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationId").value(readNotificationResponse.getNotificationId()))
                .andExpect(jsonPath("$.data.read").value(true))
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

        when(timelineService.updateNotificationAllRead()).thenReturn(readNotificationResponse);

        // when & then
        mockMvc.perform(patch("/api/notifications/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].notificationId").value(readNotificationResponse1.getNotificationId()))
                .andExpect(jsonPath("$.data[0].read").value(true))
                .andExpect(jsonPath("$.data[1].notificationId").value(readNotificationResponse2.getNotificationId()))
                .andExpect(jsonPath("$.data[1].read").value(true))
                .andExpect(jsonPath("$.message").value("모든 알림 읽음 처리 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("GET 읽지 않은 알림 개수 조회 성공")
    void get_notificationNotRead_success() throws Exception {
        // given
        Map<String, Integer> respnse = Map.of("notReadCount",2);

        when(timelineService.getNotificationNotRead()).thenReturn(respnse);

        // when & then
        mockMvc.perform(get("/api/notifications/not-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notReadCount").value(2))
                .andExpect(jsonPath("$.message").value("읽지 않은 알림 개수 조회 성공"))
                .andDo(print());
    }
}
