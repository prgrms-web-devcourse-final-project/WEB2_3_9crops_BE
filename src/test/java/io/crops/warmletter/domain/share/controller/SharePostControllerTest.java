package io.crops.warmletter.domain.share.controller;
import io.crops.warmletter.domain.share.dto.response.CursorResponse;
import io.crops.warmletter.domain.share.dto.response.ShareLetterPostResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.exception.ShareAccessException;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
        LocalDateTime fixedCreatedAt = LocalDateTime.of(2025, 2, 28, 12, 0, 0, 0);

        sharePostResponse1 = new SharePostResponse(1L, 1L, "12345", "67890", "to share my post", true, fixedCreatedAt);
        sharePostResponse2 = new SharePostResponse(2L, 2L, "12345", "67890", "to share my post1", true, fixedCreatedAt);
    }

    @Test
    @DisplayName("커서 공유 게시글 반환 ")
    void getAllPosts() throws Exception {
        // given
        List<SharePostResponse> posts = List.of(sharePostResponse1, sharePostResponse2);
        CursorResponse<SharePostResponse> cursorResponse = new CursorResponse<>(posts, 2L, true);
        when(sharePostService.getAllPosts(null, 10)).thenReturn(cursorResponse);

        // when
        mockMvc.perform(get("/api/share-posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data", hasSize(2)))
                .andExpect(jsonPath("$.data.data[0].content").value("to share my post"))
                .andExpect(jsonPath("$.data.data[1].content").value("to share my post1"))
                .andExpect(jsonPath("$.data.nextCursor").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.message").value("공유 게시글 조회 성공"))
                .andDo(print());
    }



    @Test
    @DisplayName("커서 ID 파라미터에 따라서 해당 페이지 반환 ")
    void getAllPosts_ReturnsSpecificPage() throws Exception {
        // given
        List<SharePostResponse> posts = List.of(sharePostResponse2);
        Long cursorId = 3L;
        Long nextCursorId = 2L;
        CursorResponse<SharePostResponse> cursorResponse = new CursorResponse<>(posts, nextCursorId, true);

        when(sharePostService.getAllPosts(cursorId,10)).thenReturn(cursorResponse);

        // when & then
        mockMvc.perform(get("/api/share-posts")
                        .param("cursorId", cursorId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").isArray())
                .andExpect(jsonPath("$.data.data", hasSize(1)))
                .andExpect(jsonPath("$.data.nextCursor").value(nextCursorId))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.message").value("공유 게시글 조회 성공"))
                .andDo(print());
    }
    @Test
    @DisplayName("마지막 페이지 조회 - 다음 페이지 없음")
    void getAllPosts_LastPage() throws Exception {
        // given
        List<SharePostResponse> posts = List.of(sharePostResponse2);
        Long cursorId = 3L;
        CursorResponse<SharePostResponse> cursorResponse = new CursorResponse<>(posts, null, false);

        when(sharePostService.getAllPosts(cursorId, 10)).thenReturn(cursorResponse);

        // when & then
        mockMvc.perform(get("/api/share-posts")
                        .param("cursorId", cursorId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").isArray())
                .andExpect(jsonPath("$.data.data", hasSize(1)))
                .andExpect(jsonPath("$.data.nextCursor").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.message").value("공유 게시글 조회 성공"))
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

    @Test
    @DisplayName("내가 요청한 공유 게시글 조회 성공")
    void getMySharePosts_Success() throws Exception {
        // given
        List<SharePostResponse> myPosts = List.of(sharePostResponse1, sharePostResponse2);
        when(sharePostService.getMySharePosts()).thenReturn(myPosts);

        // when & then
        mockMvc.perform(get("/api/share-posts/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].content").value("to share my post"))
                .andExpect(jsonPath("$.data[1].content").value("to share my post1"))
                .andExpect(jsonPath("$.message").value("나의 공유 게시글 조회 성공"))
                .andDo(print());

        verify(sharePostService).getMySharePosts();
    }

    @Test
    @DisplayName("내가 요청한 공유 게시글이 없으면 빈 값 반환")
    void getMySharePosts_EmptyList() throws Exception {
        // given
        when(sharePostService.getMySharePosts()).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/share-posts/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("나의 공유 게시글 조회 성공"))
                .andDo(print());

        verify(sharePostService).getMySharePosts();
    }

    @Test
    @DisplayName("공유 게시글 삭제 성공")
    void deleteSharePost_Success() throws Exception {
        // Given
        Long sharePostId = 1L;
        doNothing().when(sharePostService).deleteSharePost(sharePostId);

        // When & Then
        mockMvc.perform(delete("/api/share-posts/{sharePostId}", sharePostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제 성공"))
                .andDo(print());

        verify(sharePostService).deleteSharePost(sharePostId);
    }
    @Test
    @DisplayName("권한 없는 사용자의 게시글 삭제 시도시 예외 처리")
    void deleteSharePost_Unauthorized() throws Exception {
        // Given
        Long sharePostId = 1L;
        doThrow(new ShareAccessException())
                .when(sharePostService).deleteSharePost(sharePostId);

        // When & Then
        mockMvc.perform(delete("/api/share-posts/{sharePostId}", sharePostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SHARE-004"))
                .andExpect(jsonPath("$.message").value("해당 공유에 권한이 없습니다."))
                .andDo(print());

        verify(sharePostService).deleteSharePost(sharePostId);
    }
}