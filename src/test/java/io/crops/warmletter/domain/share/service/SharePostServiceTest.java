package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.response.ShareLetterPostResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
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
import org.springframework.data.domain.*;

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
    private Pageable pageable;

    @InjectMocks
    private SharePostService sharePostService;

    private SharePost sharePost1;
    private SharePost sharePost2;

    @BeforeEach
    void setUp() {
        // 테스트에서 사용할 객체만 생성
        sharePost1 = new SharePost(1L, "게시글1", "to share my post",true);
        sharePost2 = new SharePost(2L, "게시글2", "to share my post1",true);

    }

    @Test
    @DisplayName("활성화된 게시글 목록 페이징 조회 성공")
    void getAllPosts_ReturnsActivePosts() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<SharePost> posts = List.of(sharePost1, sharePost2);
        Page<SharePost> sharePostPage = new PageImpl<>(posts, pageable, posts.size());

        when(sharePostRepository.findAllByIsActiveTrue(pageable)).thenReturn(sharePostPage);

        // when
        Page<SharePostResponse> result = sharePostService.getAllPosts(pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).getContent()).isEqualTo("to share my post"),
                () -> assertThat(result.getContent().get(1).getContent()).isEqualTo("to share my post1"),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getNumber()).isZero()
        );

        verify(sharePostRepository).findAllByIsActiveTrue(pageable);
    }

    @Test
    @DisplayName("활성화된 게시글이 없을 경우 빈 페이지 반환")
    void getAllPosts_ReturnsEmptyPage_WhenNoActivePost() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SharePost> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(sharePostRepository.findAllByIsActiveTrue(pageable)).thenReturn(emptyPage);

        // when
        Page<SharePostResponse> result = sharePostService.getAllPosts(pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isZero(),
                () -> assertThat(result.getTotalPages()).isZero()
        );

        verify(sharePostRepository).findAllByIsActiveTrue(pageable);
    }
    @Test
    @DisplayName("두 번째 페이지 조회 성공")
    void getAllPosts_ReturnsSecondPage() {
        // given
        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<SharePost> posts = List.of(sharePost1, sharePost2);
        Page<SharePost> sharePostPage = new PageImpl<>(posts, pageable, 25); // 총 25개 중 2번째 페이지

        when(sharePostRepository.findAllByIsActiveTrue(pageable)).thenReturn(sharePostPage);

        // when
        Page<SharePostResponse> result = sharePostService.getAllPosts(pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(25),
                () -> assertThat(result.getNumber()).isEqualTo(1),
                () -> assertThat(result.getTotalPages()).isEqualTo(3)
        );

        verify(sharePostRepository).findAllByIsActiveTrue(pageable);
    }

    @Test
    @DisplayName("음수 페이지 요청시 예외 발생")
    void getAllPosts_ThrowsException_WhenPageNumberIsNegative() {
        // given
        when(pageable.getPageNumber()).thenReturn(-1);  // 음수 페이지 번호 반환하도록 설정

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> sharePostService.getAllPosts(pageable));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAGE_REQUEST);
    }

    @DisplayName("게시글 상세 조회 성공")
    @Test
    void getPostDetail_Success() {
        Long sharePostId = 1L;
        SharePostDetailResponse expectedResponse = SharePostDetailResponse.builder()
                .id(sharePostId)
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
                .id(postId)
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
                .id(sharePostId)
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



}