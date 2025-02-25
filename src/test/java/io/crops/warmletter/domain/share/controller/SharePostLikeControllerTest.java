package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.SharePostLikeRequest;
import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.service.SharePostLikeService;
import io.crops.warmletter.global.response.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharePostLikeControllerTest {

    @Mock
    private SharePostLikeService sharePostLikeService;

    @InjectMocks
    private SharePostLikeController sharePostLikeController;

    @Test
    @DisplayName("좋아요 토글 API 호출 성공")
    void toggleLike() {
        // given
        Long sharePostId = 1L;
        SharePostLikeRequest request = new SharePostLikeRequest(1L);

        // when
        ResponseEntity<BaseResponse<Void>> response =
                sharePostLikeController.toggleLike(sharePostId, request);

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("좋아요 요청 성공", response.getBody().getMessage())
        );

        verify(sharePostLikeService).toggleLike(sharePostId, request.getMemberId());
    }

    @Test
    @DisplayName("좋아요 개수 요청 호출 성공")
    void getLikeCountAndStatus() {
        // given
        Long sharePostId = 1L;
        SharePostLikeResponse mockResponse = new SharePostLikeResponse(5L, true);
        when(sharePostLikeService.getLikeCountAndStatus(sharePostId, 1L))
                .thenReturn(mockResponse);

        // when
        ResponseEntity<SharePostLikeResponse> response =
                sharePostLikeController.getLikeCountAndStatus(sharePostId);

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(5L, response.getBody().getLikeCount()),
                () -> assertEquals(true, response.getBody().isLiked())
        );

        verify(sharePostLikeService).getLikeCountAndStatus(sharePostId, 1L);
    }
}
