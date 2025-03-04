package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.config.TestConfig;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private TimelineService timeLineService;
    
    @Test
    @DisplayName("GET 타임라인 조회 성공")
    void get_timelines_success() throws Exception {
        // given
        TimelineResponse timeline1 = TimelineResponse.builder().timelineId(1L).title("1111번 편지").alarmType(AlarmType.LETTER).isRead(false).build();
        TimelineResponse timeline2 = TimelineResponse.builder().timelineId(2L).title("1111번 공유 요청").alarmType(AlarmType.SHARE).isRead(false).build();
        List<TimelineResponse> timelines = List.of(timeline2, timeline1);

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TimelineResponse> timelinePage = new PageImpl<>(timelines, pageable, timelines.size());

        when(timeLineService.getTimelines(any(Pageable.class))).thenReturn(timelinePage);

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
}
