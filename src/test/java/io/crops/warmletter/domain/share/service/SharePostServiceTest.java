package io.crops.warmletter.domain.share.service;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.dto.response.CursorResponse;
import io.crops.warmletter.domain.share.dto.response.ShareLetterPostResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.exception.ShareAccessException;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharePostServiceTest {

    @Mock
    private SharePostRepository sharePostRepository;

    @Mock
    AuthFacade authFacade;

    @InjectMocks
    private SharePostService sharePostService;

    private SharePost sharePost1;
    private SharePost sharePost2;

    @BeforeEach
    void setUp() {
        // 테스트에서 사용할 객체만 생성
        sharePost1 = new SharePost(1L,  "to share my post",true);
        sharePost2 = new SharePost(2L,  "to share my post1",true);

    }

    @Test
    @DisplayName("커서 기반 페이징: 첫 페이지 조회 성공")
    void getAllPosts_ReturnsFirstPage() {
        // given
        Long cursorId = null;
        int size = 10;

        List<SharePostResponse> responses = List.of(
                new SharePostResponse(1L, 1L, "12345", "67890", "첫 번째 게시글", true, LocalDateTime.now()),
                new SharePostResponse(2L, 2L, "13579", "24680", "두 번째 게시글", true, LocalDateTime.now().minusDays(1))
        );

        when(sharePostRepository.findAllActiveSharePostsWithZipCodes(cursorId, size + 1)).thenReturn(responses);

        // when
        CursorResponse<SharePostResponse> result = sharePostService.getAllPosts(cursorId, size);

        // then
        assertAll(
                () -> assertThat(result.getData()).hasSize(2),
                () -> assertThat(result.getData().get(0).getContent()).isEqualTo("첫 번째 게시글"),
                () -> assertThat(result.getData().get(1).getContent()).isEqualTo("두 번째 게시글"),
                () -> assertThat(result.getNextCursor()).isNull(),
                () -> assertThat(result.isHasNext()).isFalse()
        );

        verify(sharePostRepository).findAllActiveSharePostsWithZipCodes(cursorId, size + 1);
    }

    @Test
    @DisplayName("커서 기반 페이징: 다음 페이지가 있는 경우")
    void getAllPosts_WithNextPage() {
        // given
        Long cursorId = null;
        int size = 2;

        List<SharePostResponse> responses = List.of(
                new SharePostResponse(3L, 3L, "12345", "67890", "첫 번째 게시글", true, LocalDateTime.now()),
                new SharePostResponse(2L, 2L, "13579", "24680", "두 번째 게시글", true, LocalDateTime.now().minusDays(1)),
                new SharePostResponse(1L, 1L, "11111", "22222", "세 번째 게시글", true, LocalDateTime.now().minusDays(2))
        );

        when(sharePostRepository.findAllActiveSharePostsWithZipCodes(cursorId, size + 1)).thenReturn(responses);

        // when
        CursorResponse<SharePostResponse> result = sharePostService.getAllPosts(cursorId, size);

        // then
        assertAll(
                () -> assertThat(result.getData()).hasSize(2),
                () -> assertThat(result.getData().get(0).getContent()).isEqualTo("첫 번째 게시글"),
                () -> assertThat(result.getData().get(1).getContent()).isEqualTo("두 번째 게시글"),
                () -> assertThat(result.getNextCursor()).isEqualTo(2L),
                () -> assertThat(result.isHasNext()).isTrue()
        );

        verify(sharePostRepository).findAllActiveSharePostsWithZipCodes(cursorId, size + 1);
    }

    @Test
    @DisplayName("커서 기반 페이징: 마지막 페이지 조회 성공")
    void getAllPosts_LastPage() {
        // given
        Long cursorId = 3L;
        int size = 2;

        // 마지막 페이지이므로 size보다 적은 항목만 반환
        List<SharePostResponse> responses = List.of(
                new SharePostResponse(2L, 2L, "12345", "67890", "마지막 페이지 첫 번째 게시글", true, LocalDateTime.now()),
                new SharePostResponse(1L, 1L, "13579", "24680", "마지막 페이지 두 번째 게시글", true, LocalDateTime.now().minusDays(1))
        );

        when(sharePostRepository.findAllActiveSharePostsWithZipCodes(cursorId, size + 1)).thenReturn(responses);

        // when
        CursorResponse<SharePostResponse> result = sharePostService.getAllPosts(cursorId, size);

        // then
        assertAll(
                () -> assertThat(result.getData()).hasSize(2),
                () -> assertThat(result.getData().get(0).getContent()).isEqualTo("마지막 페이지 첫 번째 게시글"),
                () -> assertThat(result.getData().get(1).getContent()).isEqualTo("마지막 페이지 두 번째 게시글"),
                () -> assertThat(result.getNextCursor()).isNull(),
                () -> assertThat(result.isHasNext()).isFalse()
        );

        verify(sharePostRepository).findAllActiveSharePostsWithZipCodes(cursorId, size + 1);
    }

    @Test
    @DisplayName("커서 기반 페이징: 데이터가 없는 경우")
    void getAllPosts_EmptyData() {
        // given
        Long cursorId = null;
        int size = 10;

        when(sharePostRepository.findAllActiveSharePostsWithZipCodes(cursorId, size + 1)).thenReturn(Collections.emptyList());

        // when
        CursorResponse<SharePostResponse> result = sharePostService.getAllPosts(cursorId, size);

        // then
        assertAll(
                () -> assertThat(result.getData()).isEmpty(),
                () -> assertThat(result.getNextCursor()).isNull(),
                () -> assertThat(result.isHasNext()).isFalse()
        );

        verify(sharePostRepository).findAllActiveSharePostsWithZipCodes(cursorId, size + 1);
    }

    @DisplayName("게시글 상세 조회 성공")
    @Test
    void getPostDetail_Success() {
        Long sharePostId = 1L;
        SharePostDetailResponse expectedResponse = SharePostDetailResponse.builder()
                .sharePostId(sharePostId)
                .zipCode("10A34")
                .sharePostContent("테스트 내용")
                .build();

        when(sharePostRepository.findDetailById(sharePostId))
                .thenReturn(Optional.of(expectedResponse));

        // When
        SharePostDetailResponse actualResponse = sharePostService.getPostDetail(sharePostId);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getSharePostId()).isEqualTo(expectedResponse.getSharePostId());
        assertThat(actualResponse.getZipCode()).isEqualTo(expectedResponse.getZipCode());
        assertThat(actualResponse.getSharePostContent()).isEqualTo(expectedResponse.getSharePostContent());

        verify(sharePostRepository, times(1)).findDetailById(sharePostId);
    }

    @Test
    @DisplayName("게시글 상세 조회 시 연관된 편지들도 함께 조회")
    void getPostDetail_WithLetters() {
        // Given
        Long postId = 1L;
        List<ShareLetterPostResponse> letters = List.of(
                ShareLetterPostResponse.builder()
                        .id(1L)
                        .content("첫 번째 편지")
                        .writerZipCode("12345")
                        .receiverZipCode("67890")
                        .createdAt(LocalDateTime.now())
                        .build(),
                ShareLetterPostResponse.builder()
                        .id(2L)
                        .content("두 번째 편지")
                        .writerZipCode("11111")
                        .receiverZipCode("22222")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        SharePostDetailResponse expectedResponse = SharePostDetailResponse.builder()
                .sharePostId(postId)
                .zipCode("10A34")
                .sharePostContent("테스트 내용")
                .letters(letters)
                .build();

        when(sharePostRepository.findDetailById(postId))
                .thenReturn(Optional.of(expectedResponse));

        // When
        SharePostDetailResponse result = sharePostService.getPostDetail(postId);

        // Then
        assertThat(result.getLetters()).hasSize(2);
        assertThat(result.getLetters().get(0).getContent()).isEqualTo("첫 번째 편지");
        assertThat(result.getLetters().get(1).getContent()).isEqualTo("두 번째 편지");
        verify(sharePostRepository).findDetailById(postId);
    }

    @Test
    @DisplayName("letters 리스트가 비어있는 경우도 정상 반환")
    void getPostDetail_EmptyLetters() {
        // Given
        Long sharePostId = 1L;
        SharePostDetailResponse response = SharePostDetailResponse.builder()
                .sharePostId(sharePostId)
                .zipCode("12345")
                .sharePostContent("test message")
                .letters(Collections.emptyList())
                .build();

        when(sharePostRepository.findDetailById(sharePostId))
                .thenReturn(Optional.of(response));

        // When
        SharePostDetailResponse result = sharePostService.getPostDetail(sharePostId);

        // Then
        assertThat(result.getLetters()).isEmpty();
        verify(sharePostRepository).findDetailById(sharePostId);
    }

    @Test
    @DisplayName("게시글이 존재하지 않는 경우 예외 발생")
    void getPostDetail_NotFound() {
        // Given
        Long sharePostId = 1L;
        when(sharePostRepository.findDetailById(sharePostId))
                .thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> sharePostService.getPostDetail(sharePostId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHARE_POST_NOT_FOUND);
        verify(sharePostRepository).findDetailById(sharePostId);
    }


    @Test
    @DisplayName("내가 요청한 활성화된 공유 게시글 조회 성공")
    void getMySharePosts_ReturnsRequestedActivePosts() {
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        List<SharePostResponse> expectedResponses = List.of(
                new SharePostResponse(1L, 1L, "12345", "67890", "내가 요청한 게시물 1", true, LocalDateTime.now()),
                new SharePostResponse(2L, 2L, "12345", "24680", "내가 요청한 게시물 2", true, LocalDateTime.now().minusDays(1))
        );

        when(sharePostRepository.findMyRequestedActiveSharePosts(memberId)).thenReturn(expectedResponses);

        List<SharePostResponse> result = sharePostService.getMySharePosts();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).getContent()).isEqualTo("내가 요청한 게시물 1"),
                () -> assertThat(result.get(1).getContent()).isEqualTo("내가 요청한 게시물 2"),
                () -> assertThat(result.get(0).getWriterZipCode()).isEqualTo("12345"),
                () -> assertThat(result.get(1).getWriterZipCode()).isEqualTo("12345")
        );

        verify(authFacade).getCurrentUserId();
        verify(sharePostRepository).findMyRequestedActiveSharePosts(memberId);
    }

    @Test
    @DisplayName("내가 요청한 활성화된 공유 게시글이 없을 경우 빈 값 반환")
    void getMySharePosts_ReturnsEmptyList_WhenNoRequestedActivePost() {
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(sharePostRepository.findMyRequestedActiveSharePosts(memberId)).thenReturn(Collections.emptyList());

        List<SharePostResponse> result = sharePostService.getMySharePosts();

        assertThat(result).isEmpty();
        verify(authFacade).getCurrentUserId();
        verify(sharePostRepository).findMyRequestedActiveSharePosts(memberId);
    }

    @Test
    @DisplayName("공유 게시글 삭제 성공")
    void deleteSharePost_Success() {
        // Given
        Long sharePostId = 1L;
        Long currentUserId = 10L;

        SharePost sharePost = new SharePost(1L, "테스트 게시글", true);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(sharePostRepository.findByIdAndRequesterId(sharePostId, currentUserId))
                .thenReturn(Optional.of(sharePost));

        // When
        sharePostService.deleteSharePost(sharePostId);

        // Then
        assertFalse(sharePost.isActive());
        verify(authFacade).getCurrentUserId();
        verify(sharePostRepository).findByIdAndRequesterId(sharePostId, currentUserId);
    }

    @Test
    @DisplayName("권한 없는 사용자의 게시글 삭제시 예외 발생")
    void deleteSharePost_ThrowsException_WhenUserUnauthorized() {
        // Given
        Long sharePostId = 1L;
        Long currentUserId = 10L;

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(sharePostRepository.findByIdAndRequesterId(sharePostId, currentUserId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ShareAccessException.class,
                () -> sharePostService.deleteSharePost(sharePostId));

        verify(authFacade).getCurrentUserId();
        verify(sharePostRepository).findByIdAndRequesterId(sharePostId, currentUserId);
    }
}