package io.crops.warmletter.domain.timeline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TimelineService timeLineService;
    
    @Test
    @DisplayName("GET 타임라인 조회 성공")
    void get_timelines_success() throws Exception {
        // given
        List<TimelineResponse> timelines = new ArrayList<>();
        TimelineResponse timeline1 = TimelineResponse.builder().timelineId(1).title("1111번 편지").alarmType(AlarmType.LETTER).isRead(false).build();
        TimelineResponse timeline2 = TimelineResponse.builder().timelineId(2).title("1111번 공유 요청").alarmType(AlarmType.SHARE).isRead(false).build();
        timelines.add(timeline1);
        timelines.add(timeline2);

        when(timeLineService.getTimelines()).thenReturn(timelines);

        // when & then
        mockMvc.perform(get("/api/timelines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].timelineId").value(timeline1.getTimelineId()))
                .andExpect(jsonPath("$.data[0].title").value(timeline1.getTitle()))
                .andExpect(jsonPath("$.data[0].alarmType").value(timeline1.getAlarmType().toString()))
                .andExpect(jsonPath("$.data[0].read").value(timeline1.isRead()))
                .andExpect(jsonPath("$.data[1].timelineId").value(timeline2.getTimelineId()))
                .andExpect(jsonPath("$.data[1].title").value(timeline2.getTitle()))
                .andExpect(jsonPath("$.data[1].alarmType").value(timeline2.getAlarmType().toString()))
                .andExpect(jsonPath("$.data[1].read").value(timeline2.isRead()))
                .andExpect(jsonPath("$.message").value("타임라인 조회 성공"))
                .andDo(print());
    }
}
