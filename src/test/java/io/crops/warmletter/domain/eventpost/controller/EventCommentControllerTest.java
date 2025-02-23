package io.crops.warmletter.domain.eventpost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.service.EventCommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
@SpringBootTest
class EventCommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventCommentService eventCommentService;

    @Test
    @DisplayName("POST 게시판 댓글 생성 성공")
    void create_eventComment_success() throws Exception {
        // given
        long eventPostId = 1L;

        CreateEventCommentRequest createEventCommentRequest = CreateEventCommentRequest.builder()
                .content("내용")
                .build();

        EventCommentResponse eventCommentResponse = EventCommentResponse.builder()
                .commentId(1L)
                .content(createEventCommentRequest.getContent())
                .build();

        when(eventCommentService.createEventComment(any(),anyLong())).thenReturn(eventCommentResponse);

        // when & then
        mockMvc.perform(post("/api/event-posts/{eventPostId}/comments",eventPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(1L)) // 반환된 eventPostId 검증
                .andExpect(jsonPath("$.data.content").value("내용")) // 반환된 title 검증
                .andExpect(jsonPath("$.message").value("댓글 생성 성공")) // 반환된 message 검증
                .andDo(print());
    }

    @Test
    @DisplayName("POST 게시판 댓글 생성 실패 - content 값이 없음")
    void create_eventComment_notExistContent () throws Exception {
        //given
        CreateEventCommentRequest createEventCommentRequest = CreateEventCommentRequest.builder()
                .content("")
                .build();

        //when & then
        mockMvc.perform(post("/api/event-posts/{eventPostId}/comments",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용을 입력해주세요."))
                .andDo(print());
    }


}