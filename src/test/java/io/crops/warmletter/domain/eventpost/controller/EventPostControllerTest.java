package io.crops.warmletter.domain.eventpost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentsResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.exception.EventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
@SpringBootTest
class EventPostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventPostService eventPostService;

    @Test
    @DisplayName("POST 게시판 생성 성공")
    void create_event_post_success() throws Exception {
        // given
        CreateEventPostRequest createEventPostRequest = CreateEventPostRequest.builder()
                .title("제목")
                .build();

        EventPostResponse eventPostResponse = EventPostResponse.builder()
                .eventPostId(1L)
                .title("제목")
                .build();

        when(eventPostService.createEventPost(any(CreateEventPostRequest.class)))
                .thenReturn(eventPostResponse);

        // when & then
        mockMvc.perform(post("/api/admin/event-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventPostRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventPostId").value(1L)) // 반환된 eventPostId 검증
                .andExpect(jsonPath("$.data.title").value("제목")) // 반환된 title 검증
                .andExpect(jsonPath("$.message").value("게시판 생성 성공")) // 반환된 message 검증
                .andDo(print());
    }

    @Test
    @DisplayName("POST 게시판 생성 실패 - title 값이 없음")
    void create_event_post_not_exist_title () throws Exception {
        //given
        CreateEventPostRequest createEventPostRequest = CreateEventPostRequest.builder()
                .title("")
                .build();

        //when & then
        mockMvc.perform(post("/api/admin/event-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventPostRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목을 입력해주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("DELETE 게시판 삭제 성공")
    void delete_event_post_success() throws Exception {
        // given
        long eventPostId = 1L;

        when(eventPostService.deleteEventPost(eventPostId)).thenReturn(Map.of("eventPostId", eventPostId));

        // when & then
        mockMvc.perform(delete("/api/admin/event-posts/{eventPostId}", eventPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시판 삭제 성공")) // 성공 메시지 검증
                .andExpect(jsonPath("$.data.eventPostId").value(eventPostId)) // 삭제된 eventPostId 검증
                .andDo(print());
    }

    @Test
    @DisplayName("DELETE 게시판 삭제 실패 - 일치하는 eventPostId 없음")
    void delete_event_post_not_found() throws Exception {
        // given
        long eventPostId = 999L;

        when(eventPostService.deleteEventPost(eventPostId)).thenThrow(new EventPostNotFoundException());

        // when & then
        mockMvc.perform(delete("/api/admin/event-posts/{eventPostId}", eventPostId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 이벤트 게시글을 찾을 수 없습니다.")) // 실패 메시지 검증
                .andDo(print());
    }

    @Test
    @DisplayName("GET 사용중인 게시판 조회 성공")
    void get_used_event_post_success() throws Exception {
        EventPostResponse eventPostResponse = EventPostResponse.builder()
                .eventPostId(1L)
                .title("제목")
                .build();

        // given
        when(eventPostService.getUsedEventPost()).thenReturn(eventPostResponse);

        // when & then
        mockMvc.perform(get("/api/event-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventPostId").value(1L))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.message").value("게시판 조회(사용중) 성공")) // 실패 메시지 검증
                .andDo(print());
    }

    @Test
    @DisplayName("GET 게시판 개별 조회 성공")
    void get_event_post_success() throws Exception {
        long eventPostId = 1L;

        List<EventCommentsResponse> comments = new ArrayList<>();
        EventCommentsResponse comment1 = EventCommentsResponse.builder().commentId(1).zipCode("11111").content("내용1").build();
        EventCommentsResponse comment2 = EventCommentsResponse.builder().commentId(2).zipCode("22222").content("내용2").build();
        comments.add(comment1);
        comments.add(comment2);

        EventPostDetailResponse eventPostDetailResponse = EventPostDetailResponse.builder()
                .eventPostId(1L)
                .title("제목")
                .eventPostComments(comments)
                .build();

        // given
        when(eventPostService.getEventPostDetail(eventPostId)).thenReturn(eventPostDetailResponse);

        // when & then
        mockMvc.perform(get("/api/event-posts/{eventPostId}", eventPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventPostId").value(1L))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.eventPostComments[0].commentId").value(1L))
                .andExpect(jsonPath("$.data.eventPostComments[0].zipCode").value("11111"))
                .andExpect(jsonPath("$.data.eventPostComments[0].content").value("내용1"))
                .andExpect(jsonPath("$.data.eventPostComments[1].commentId").value(2L))
                .andExpect(jsonPath("$.data.eventPostComments[1].zipCode").value("22222"))
                .andExpect(jsonPath("$.data.eventPostComments[1].content").value("내용2"))
                .andExpect(jsonPath("$.message").value("게시판 조회(개별) 성공")) // 실패 메시지 검증
                .andDo(print());
    }



}