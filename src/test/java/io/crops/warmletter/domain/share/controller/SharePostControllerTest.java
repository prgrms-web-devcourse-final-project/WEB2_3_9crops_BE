package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.response.ShareLetterPostResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.service.SharePostService;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import io.crops.warmletter.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SharePostController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class SharePostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SharePostService sharePostService;



    private SharePostResponse sharePostResponse1;
    private SharePostResponse sharePostResponse2;
    @BeforeEach
    void createSharePost() {
        SharePost sharePost = new SharePost(1L,  "to share my post",true);
        SharePost sharePost1 = new SharePost(2L, "to share my post1",true);
        sharePostResponse1 = new SharePostResponse(sharePost);
        sharePostResponse2 = new SharePostResponse(sharePost1);
    }

    @Test
    @DisplayName("페이징된 공유 게시글 반환 ")
    void getAllPosts() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
        List<SharePostResponse> posts = List.of(sharePostResponse1, sharePostResponse2);
        Page<SharePostResponse> postPage = new PageImpl<>(posts, pageable, posts.size());
        when(sharePostService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

        //when
        mockMvc.perform(get("/api/share-posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(2)))
                .andExpect(jsonPath("$.content[0].content").value("to share my post"))
                .andExpect(jsonPath("$.content[1].content").value("to share my post1"))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andDo(print());
    }

    @Test
    @DisplayName("페이지 파라미터에 따라서 해당 페이지 반환 ")
    void getAllPosts_ReturnsSpecificPage() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")); // 수정: 1 -> 0
        Page<SharePostResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 20);

        when(sharePostService.getAllPosts(any(Pageable.class))).thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/share-posts")
                        .param("page", "1") // 프론트엔드는 여전히 1을 보냄
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.currentPage").value(1)) // 수정: 2 -> 1 (BackendPage 0 + 1)
                .andExpect(jsonPath("$.totalElements").value(20))
                .andDo(print());
    }


    @Test
    @DisplayName("음수 페이지 요청시 예외 발생")
    void getAllPosts_ThrowsException_WhenPageNumberIsNegative() throws Exception {
        // given
        when(sharePostService.getAllPosts(any(Pageable.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_PAGE_REQUEST));

        // when & then
        mockMvc.perform(get("/api/share-posts")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PAGE_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PAGE_REQUEST.getMessage()))
                .andDo(print());
    }

    @DisplayName("공유 게시글 상세 조회")
    @Test
    void getPostDetailTest() throws Exception {
        // given
        ShareLetterPostResponse letterResponse = ShareLetterPostResponse.builder()
                .id(10L)
                .content("편지 내용입니다")
                .writerZipCode("12345")
                .receiverZipCode("11112345")
                .createdAt(LocalDateTime.now())
                .build();

        SharePostDetailResponse sharePostDetailResponse = SharePostDetailResponse.builder()
                .sharePostId(1L)
                .zipCode("10A32")
                .sharePostContent("hello")
                .letters(Collections.singletonList(letterResponse))
                .build();

        when(sharePostService.getPostDetail(sharePostDetailResponse.getSharePostId()))
                .thenReturn(sharePostDetailResponse);

        // when and then
        mockMvc.perform(get("/api/share-posts/" + sharePostDetailResponse.getSharePostId())  // API 경로 수정
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sharePostId").value(1L))
                .andExpect(jsonPath("$.data.zipCode").value("10A32"))
                .andExpect(jsonPath("$.data.sharePostContent").value("hello"))
                .andExpect(jsonPath("$.data.letters[0].id").value(10L))
                .andExpect(jsonPath("$.data.letters[0].content").value("편지 내용입니다"))
                .andExpect(jsonPath("$.data.letters[0].writerZipCode").value("12345"))
                .andExpect(jsonPath("$.data.letters[0].receiverZipCode").value("11112345"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andDo(print());

        verify(sharePostService).getPostDetail(sharePostDetailResponse.getSharePostId());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 실패")
    void getPostDetail_NotFound() throws Exception {
        // Given
        Long sharePostId = 999L;
        doThrow(new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND))
                .when(sharePostService).getPostDetail(sharePostId);

        // When & Then
        mockMvc.perform(get("/api/share-posts/{sharePostId}", sharePostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SHARE-002"))
                .andExpect(jsonPath("$.message").value("해당 공유 게시글을 찾을 수 없습니다."))
                .andDo(print());

        verify(sharePostService).getPostDetail(sharePostId);
    }

}