package io.crops.warmletter.domain.eventpost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.*;
import io.crops.warmletter.domain.eventpost.exception.EventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
import io.crops.warmletter.global.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
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
    @DisplayName("GET 게시판 전체 조회 성공 - pageNumber < 0")
    void get_eventPostsNegative_success() throws Exception {
        //given
        EventPostsResponse eventPostsResponse1 = EventPostsResponse.builder().eventPostId(1L).title("제목").build();
        EventPostsResponse eventPostsResponse2 = EventPostsResponse.builder().eventPostId(2L).title("제목").build();
        ReflectionTestUtils.setField(eventPostsResponse2, "isUsed", true);

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<EventPostsResponse> eventPosts = List.of(eventPostsResponse2, eventPostsResponse1);
        Page<EventPostsResponse> eventPostsPage = new PageImpl<>(eventPosts, pageable, eventPosts.size());
        when(eventPostService.getEventPosts(any(Pageable.class))).thenReturn(eventPostsPage);

        //when & then
        mockMvc.perform(get("/api/admin/event-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].eventPostId").value(eventPostsResponse2.getEventPostId()))
                .andExpect(jsonPath("$.data.content[0].title").value(eventPostsResponse2.getTitle()))
                .andExpect(jsonPath("$.data.content[0].used").value(eventPostsResponse2.isUsed()))
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.message").value("게시판 조회(전체) 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("GET 게시판 전체 조회 성공 - pageNumber > 0")
    void get_eventPostsPageNumberPositive_success() throws Exception {
        //given
        EventPostsResponse eventPostsResponse1 = EventPostsResponse.builder().eventPostId(1L).title("제목").build();
        EventPostsResponse eventPostsResponse2 = EventPostsResponse.builder().eventPostId(2L).title("제목").build();
        ReflectionTestUtils.setField(eventPostsResponse2, "isUsed", true);

        Pageable pageable = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<EventPostsResponse> eventPosts = List.of(eventPostsResponse2, eventPostsResponse1);
        Page<EventPostsResponse> eventPostsPage = new PageImpl<>(eventPosts, pageable, eventPosts.size());
        when(eventPostService.getEventPosts(any(Pageable.class))).thenReturn(eventPostsPage);

        //when & then
        mockMvc.perform(get("/api/admin/event-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.currentPage").value(2))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.message").value("게시판 조회(전체) 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("POST 게시판 생성 성공")
    void create_eventPost_success() throws Exception {
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
                .andExpect(jsonPath("$.data.eventPostId").value(1L))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.message").value("게시판 생성 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("POST 게시판 생성 실패 - title 값이 없음")
    void create_eventPost_notExistTitle () throws Exception {
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
    void delete_eventPost_success() throws Exception {
        // given
        Long eventPostId = 1L;

        when(eventPostService.deleteEventPost(eventPostId)).thenReturn(Map.of("eventPostId", eventPostId));

        // when & then
        mockMvc.perform(delete("/api/admin/event-posts/{eventPostId}", eventPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시판 삭제 성공"))
                .andExpect(jsonPath("$.data.eventPostId").value(eventPostId))
                .andDo(print());
    }

    @Test
    @DisplayName("DELETE 게시판 삭제 실패 - 일치하는 eventPostId 없음")
    void delete_eventPost_notFound() throws Exception {
        // given
        Long eventPostId = 999L;

        when(eventPostService.deleteEventPost(eventPostId)).thenThrow(new EventPostNotFoundException());

        // when & then
        mockMvc.perform(delete("/api/admin/event-posts/{eventPostId}", eventPostId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 이벤트 게시글을 찾을 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("GET 사용중인 게시판 조회 성공")
    void get_usedEventPost_success() throws Exception {
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
                .andExpect(jsonPath("$.message").value("게시판 조회(사용중) 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("GET 게시판 개별 조회 성공")
    void get_eventPost_success() throws Exception {
        // given
        Long eventPostId = 1L;
        EventCommentsResponse comment1 = EventCommentsResponse.builder().commentId(1L).zipCode("11111").content("내용1").build();
        EventCommentsResponse comment2 = EventCommentsResponse.builder().commentId(2L).zipCode("22222").content("내용2").build();

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<EventCommentsResponse> comments = List.of(comment2, comment1);

        PageResponse<EventCommentsResponse> eventCommentsResponse = new PageResponse<>(
                new PageImpl<>(comments, pageable, comments.size()));

        EventPostDetailResponse eventPostDetailResponse = EventPostDetailResponse.builder()
                .eventPostId(1L)
                .title("제목")
                .eventPostComments(eventCommentsResponse)
                .build();

        when(eventPostService.getEventPostDetail(any(Long.class),any(Pageable.class))).thenReturn(eventPostDetailResponse);

        // when & then
        mockMvc.perform(get("/api/event-posts/{eventPostId}", eventPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventPostId").value(1L))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.eventPostComments.content", hasSize(2)))
                .andExpect(jsonPath("$.data.eventPostComments.content[0].commentId").value(comment2.getCommentId()))
                .andExpect(jsonPath("$.data.eventPostComments.content[0].zipCode").value(comment2.getZipCode()))
                .andExpect(jsonPath("$.data.eventPostComments.content[0].content").value(comment2.getContent()))
                .andExpect(jsonPath("$.data.eventPostComments.currentPage").value(1))
                .andExpect(jsonPath("$.data.eventPostComments.size").value(1))
                .andExpect(jsonPath("$.data.eventPostComments.totalElements").value(2))
                .andExpect(jsonPath("$.data.eventPostComments.totalPages").value(2))
                .andExpect(jsonPath("$.message").value("게시판 조회(개별) 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("PATCH 게시판 사용여부 변경 성공 - true에서 false")
    void update_eventPostIsUsedToFalse_success() throws Exception {
        // given
        Long eventPostId = 1L;

        EventPostStatusResponse eventPostStatusResponse = EventPostStatusResponse.builder()
                .eventPostId(1L)
                .isUsed(false).build();

        when(eventPostService.updateEventPostIsUsed(eventPostId)).thenReturn(eventPostStatusResponse);

        // when & then
        mockMvc.perform(patch("/api/admin/event-posts/{eventPostId}/status", eventPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventPostId").value(eventPostStatusResponse.getEventPostId()))
                .andExpect(jsonPath("$.data.used").value(eventPostStatusResponse.isUsed()))
                .andExpect(jsonPath("$.message").value("게시판 사용여부 변경 성공"))
                .andDo(print());
    }

}