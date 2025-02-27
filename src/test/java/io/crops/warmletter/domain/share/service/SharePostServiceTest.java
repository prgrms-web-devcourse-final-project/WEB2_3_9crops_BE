package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.share.dto.response.ShareLetterPostResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.exception.SharePageException;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import io.crops.warmletter.global.util.PageableConverter;
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
    @DisplayName("활성화된 게시글 목록 페이징 조회 성공")
    void getAllPosts_ReturnsActivePosts() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<SharePostResponse> responses = List.of(
                new SharePostResponse(1L, 1L, "12345", "67890", "to share my post", true, LocalDateTime.now()),
                new SharePostResponse(2L, 2L, "13579", "24680", "to share my post1", true, LocalDateTime.now().minusDays(1))
        );

        Page<SharePostResponse> responsePage = new PageImpl<>(responses, pageable, responses.size());

        when(sharePostRepository.findAllActiveSharePostsWithZipCodes(pageable)).thenReturn(responsePage);

        // when
        Page<SharePostResponse> result = sharePostService.getAllPosts(pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).getContent()).isEqualTo("to share my post"),
                () -> assertThat(result.getContent().get(1).getContent()).isEqualTo("to share my post1"),
                () -> assertThat(result.getContent().get(0).getWriterZipCode()).isEqualTo("12345"),
                () -> assertThat(result.getContent().get(0).getReceiverZipCode()).isEqualTo("67890"),
                () -> assertThat(result.getContent().get(1).getWriterZipCode()).isEqualTo("13579"),
                () -> assertThat(result.getContent().get(1).getReceiverZipCode()).isEqualTo("24680"),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getNumber()).isZero()
        );

        verify(sharePostRepository).findAllActiveSharePostsWithZipCodes(pageable);
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
    @DisplayName("음수 페이지 요청시 예외 발생")
    void getAllPosts_ThrowsException_WhenPageNumberIsNegative() {
        // given
        Pageable mockPageable = mock(Pageable.class);
        when(mockPageable.getPageNumber()).thenReturn(-1);
        // 불필요한 스터빙 제거

        // when & then
        SharePageException exception = assertThrows(SharePageException.class,
                () -> sharePostService.getAllPosts(mockPageable));

        // getCurrentUser가 호출되었는지만 확인
        verify(authFacade).getCurrentUser();
        // repository는 호출되지 않아야 함
        verify(sharePostRepository, never()).findAllActiveSharePostsWithZipCodes(any());
    }
    @Test
    @DisplayName("authFacade.getCurrentUser() 호출 확인 - getAllPosts")
    void getAllPosts_CallsGetCurrentUser() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<SharePostResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(sharePostRepository.findAllActiveSharePostsWithZipCodes(pageable)).thenReturn(emptyPage);

        // when
        sharePostService.getAllPosts(pageable);

        // then
        verify(authFacade).getCurrentUser();
    }
    @Test
    @DisplayName("authFacade.getCurrentUser() 호출 확인 - getPostDetail")
    void getPostDetail_CallsGetCurrentUser() {
        // given
        Long postId = 1L;
        SharePostDetailResponse response = SharePostDetailResponse.builder()
                .sharePostId(postId)
                .build();

        when(sharePostRepository.findDetailById(postId)).thenReturn(Optional.of(response));

        // when
        sharePostService.getPostDetail(postId);

        // then
        verify(authFacade).getCurrentUser();
    }

    @Test
    @DisplayName("두 번째 페이지 조회 성공")
    void getAllPosts_ReturnsSecondPage() {
        // given
        Pageable requestPageable = PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Pageable convertedPageable = PageableConverter.convertToPageable(requestPageable); // 2 -> 1 변환

        List<SharePostResponse> responses = List.of(
                new SharePostResponse(1L, 1L, "12345", "67890", "to share my post", true, LocalDateTime.now()),
                new SharePostResponse(2L, 2L, "13579", "24680", "to share my post1", true, LocalDateTime.now().minusDays(1))
        );

        Page<SharePostResponse> responsePage = new PageImpl<>(responses, convertedPageable, 25); // 총 25개 중 2번째 페이지

        when(sharePostRepository.findAllActiveSharePostsWithZipCodes(any())).thenReturn(responsePage);

        // when
        Page<SharePostResponse> result = sharePostService.getAllPosts(requestPageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(25),
                () -> assertThat(result.getNumber()).isEqualTo(1), // convertedPageable의 페이지 번호
                () -> assertThat(result.getTotalPages()).isEqualTo(3)
        );

        verify(authFacade).getCurrentUser();
        verify(sharePostRepository).findAllActiveSharePostsWithZipCodes(any());
    }
}