package io.crops.warmletter.domain.share.service;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareInboxResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.domain.share.exception.ShareProposalNotFoundException;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.domain.share.repository.ShareProposalLetterRepository;
import io.crops.warmletter.domain.share.repository.ShareProposalRepository;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareProposalServiceTest {

    @Mock
    private ShareProposalRepository shareProposalRepository;

    @Mock
    private ShareProposalLetterRepository shareProposalLetterRepository;

    @Mock
    private SharePostRepository sharePostRepository;

    @Mock
    private NotificationFacade notificationFacade;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private ShareProposalService shareProposalService;

    @Test
    @DisplayName("공유 제안 요청 성공")
    void requestShareProposal_Success() {
        // given
        Long requesterId = 1L;
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                2L,
                "공유 요청"
        );

        ShareProposal shareProposal = request.toEntity(requesterId);
        ReflectionTestUtils.setField(shareProposal, "id", 1L);

        ShareProposalResponse expectedResponse = ShareProposalResponse.builder().shareProposalId(1L).zipCode("12345").build();

        when(authFacade.getCurrentUserId()).thenReturn(requesterId);
        when(shareProposalRepository.save(any(ShareProposal.class))).thenReturn(shareProposal);
        when(shareProposalRepository.findShareProposalWithZipCode(anyLong())).thenReturn(expectedResponse);

        ShareProposalResponse response = shareProposalService.requestShareProposal(request);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(expectedResponse.getShareProposalId(), response.getShareProposalId()),
                () -> assertEquals(expectedResponse.getZipCode(), response.getZipCode())
        );

        verify(authFacade).getCurrentUserId(); // AuthFacade 호출 검증 추가
        verify(shareProposalRepository).save(any(ShareProposal.class));
        verify(shareProposalLetterRepository).saveAll(anyList());
        verify(shareProposalRepository).findShareProposalWithZipCode(anyLong());
    }


    @Test
    @DisplayName("편지 ID 목록이 유효하지 않은 경우 예외 발생")
    void requestShareProposal_ThrowsException_WhenLetterIdsInvalid() {
        // given
        Long currentUserId = 1L;

        ShareProposalRequest request = new ShareProposalRequest(
                null, // 편지 ID 목록이 null
                2L,
                "공유 요청"
        );

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);

        // when & then
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                shareProposalService.requestShareProposal(request));

        verify(authFacade).getCurrentUserId();
    }


    @Test
    @DisplayName("빈 편지 목록으로 요청시 처리되어야 함")
    void requestShareProposal_WithEmptyLetters() {
        // given
        Long currentUserId = 1L;
        ShareProposalRequest request = mock(ShareProposalRequest.class);

        when(request.getLetterIds()).thenReturn(Collections.emptyList());

        ShareProposal mockProposal = mock(ShareProposal.class);
        when(request.toEntity(currentUserId)).thenReturn(mockProposal);

        ShareProposal savedProposal = mock(ShareProposal.class);
        when(savedProposal.getId()).thenReturn(1L);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.save(any(ShareProposal.class))).thenReturn(savedProposal);
        when(shareProposalRepository.findShareProposalWithZipCode(anyLong()))
                .thenReturn(ShareProposalResponse.builder().shareProposalId(1L).build());

        ShareProposalResponse response = shareProposalService.requestShareProposal(request);

        assertNotNull(response);

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).save(any(ShareProposal.class));
        verify(shareProposalLetterRepository).saveAll(Collections.emptyList()); // 빈 리스트가 전달됨
        verify(shareProposalRepository).findShareProposalWithZipCode(anyLong());
    }

    @Test
    @DisplayName("필수값(recipientId) 누락시 예외 발생")
    void requestShareProposal_WithoutRecipientId() {
        // given
        Long currentUserId = 1L;
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                null,  // recipientId null
                "공유 요청"
        );

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);

        assertThrows(NullPointerException.class,
                () -> shareProposalService.requestShareProposal(request));

        verify(authFacade).getCurrentUserId();
    }



    @Test
    @DisplayName("Response가 null일 경우 예외 발생")
    void requestShareProposal_ResponseNotFound() {
        // given
        Long currentUserId = 1L;
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                2L,
                "공유 요청"
        );

        ShareProposal shareProposal = request.toEntity(currentUserId);
        ReflectionTestUtils.setField(shareProposal, "id", 1L);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.save(any(ShareProposal.class))).thenReturn(shareProposal);
        when(shareProposalRepository.findShareProposalWithZipCode(anyLong())).thenReturn(null);

        // when & then
        ShareProposalNotFoundException exception = assertThrows(ShareProposalNotFoundException.class,
                () -> shareProposalService.requestShareProposal(request));

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).save(any(ShareProposal.class));
        verify(shareProposalLetterRepository).saveAll(anyList());
        verify(shareProposalRepository).findShareProposalWithZipCode(anyLong());
    }

    @Test
    @DisplayName("Letter 저장 실패시 예외 발생")
    void requestShareProposal_LetterSaveFail() {
        // given
        Long currentUserId = 1L;
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                2L,
                "공유 요청"
        );

        ShareProposal shareProposal = request.toEntity(currentUserId);
        ReflectionTestUtils.setField(shareProposal, "id", 1L);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.save(any(ShareProposal.class))).thenReturn(shareProposal);
        when(shareProposalLetterRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("저장 실패"));

        // when & then
        assertThrows(RuntimeException.class,
                () -> shareProposalService.requestShareProposal(request));

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).save(any(ShareProposal.class));
    }

    @Test
    @DisplayName("존재하지 않는 공유 제안 승인 실패")
    void approveShareProposal_NotFound() {
        // given
        Long shareProposalId = 999L;

        // when
        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> shareProposalService.approveShareProposal(shareProposalId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHARE_PROPOSAL_NOTFOUND);

        verify(shareProposalRepository).findById(shareProposalId);
        verify(sharePostRepository, never()).save(any(SharePost.class));
    }


    @Test
    @DisplayName("공유 제안 승인 성공")
    void approveShareProposal_Success() {
        Long shareProposalId = 1L;
        Long recipientId = 2L;
        Long currentUserId = recipientId; // 현재 사용자가 수신자와 동일하게 설정
        Long sharePostId = 1L;

        ShareProposal shareProposal = mock(ShareProposal.class);
        when(shareProposal.getId()).thenReturn(shareProposalId);
        when(shareProposal.getRecipientId()).thenReturn(recipientId);
        when(shareProposal.getMessage()).thenReturn("test message");
        when(shareProposal.getStatus()).thenReturn(ProposalStatus.APPROVED);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);

        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.of(shareProposal));

        ArgumentCaptor<SharePost> sharePostCaptor = ArgumentCaptor.forClass(SharePost.class);
        when(sharePostRepository.save(sharePostCaptor.capture())).thenAnswer(invocation -> {
            SharePost savedPost = sharePostCaptor.getValue();
            // 저장 시 ID 설정 시뮬레이션
            ReflectionTestUtils.setField(savedPost, "id", sharePostId);
            return savedPost;
        });

        // when
        ShareProposalStatusResponse response = shareProposalService.approveShareProposal(shareProposalId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getShareProposalId()).isEqualTo(shareProposalId);
        verify(notificationFacade, times(2)).sendNotification(any(), any(), any(), any());
        assertThat(response.getStatus()).isEqualTo(ProposalStatus.APPROVED);
        assertThat(response.getSharePostId()).isEqualTo(sharePostId);

        // 검증
        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).findById(shareProposalId);
        verify(sharePostRepository).save(any(SharePost.class));
        verify(shareProposal).updateStatus(ProposalStatus.APPROVED);

        SharePost createdPost = sharePostCaptor.getValue();
        assertThat(createdPost.getShareProposalId()).isEqualTo(shareProposalId);
        assertThat(createdPost.getContent()).isEqualTo("test message");
        assertThat(createdPost.isActive()).isTrue();
    }

    @Test
    @DisplayName("공유 제안 거절 성공")
    void rejectShareProposal_Success() {
        // given
        Long shareProposalId = 1L;
        Long requesterId = 1L;
        Long recipientId = 2L;
        Long currentUserId = recipientId; // 현재 사용자는 수신자여야 함

        ShareProposal shareProposal = new ShareProposal(requesterId, recipientId, "test");
        ReflectionTestUtils.setField(shareProposal, "id", shareProposalId);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.of(shareProposal));

        ShareProposalStatusResponse response = shareProposalService.rejectShareProposal(shareProposalId);

        assertThat(response.getShareProposalId()).isEqualTo(shareProposalId);
        assertThat(response.getStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(shareProposal.getStatus()).isEqualTo(ProposalStatus.REJECTED);

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).findById(shareProposalId);
    }

    @Test
    @DisplayName("공유 제안 거절 실패 - 존재하지 않는 제안")
    void rejectShareProposal_NotFound() {
        // given
        Long shareProposalId = 1L;
        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> shareProposalService.rejectShareProposal(shareProposalId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHARE_PROPOSAL_NOTFOUND);
    }

    @Test
    @DisplayName("공유 제안 거절 테스트 - 예외 타입 수정")
    void rejectShareProposal_NotFound_Updated() {
        // given
        Long shareProposalId = 1L;
        Long currentUserId = 1L;

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(ShareProposalNotFoundException.class,
                () -> shareProposalService.rejectShareProposal(shareProposalId));

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).findById(shareProposalId);
    }
    @Test
    @DisplayName("공유 제안 승인 실패 - 권한 없음")
    void approveShareProposal_NoPermission() {
        // given
        Long shareProposalId = 1L;
        Long recipientId = 2L;
        Long currentUserId = 3L; // 현재 사용자가 수신자와 다른 ID

        ShareProposal shareProposal = mock(ShareProposal.class);
        // getId()는 호출되지 않으므로 제거
        when(shareProposal.getRecipientId()).thenReturn(recipientId);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.of(shareProposal));

        // when & then
        assertThrows(ShareProposalNotFoundException.class,
                () -> shareProposalService.approveShareProposal(shareProposalId));

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).findById(shareProposalId);
        verify(sharePostRepository, never()).save(any(SharePost.class));
        verify(shareProposal, never()).updateStatus(any(ProposalStatus.class));
    }
    @Test
    @DisplayName("요청받은 공유 내역 조회 성공")
    void getReceivedShareProposals_Success() {
        // Given
        Long currentUserId = 1L;
        List<ShareInboxResponse> expectedResponses = List.of(
                ShareInboxResponse.builder()
                        .shareProposalId(1L)
                        .requesterZipCode("12345")
                        .recipientZipCode("67890")
                        .message("첫 번째 공유 요청입니다")
                        .status(ProposalStatus.PENDING)
                        .build(),
                ShareInboxResponse.builder()
                        .shareProposalId(2L)
                        .requesterZipCode("23456")
                        .recipientZipCode("67890")
                        .message("두 번째 공유 요청입니다")
                        .status(ProposalStatus.APPROVED)
                        .build()
        );

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.getAllByRecipientIdOrderByCreatedAtDesc(currentUserId))
                .thenReturn(expectedResponses);

        // When
        List<ShareInboxResponse> responses = shareProposalService.getReceivedShareProposals();

        // Then
        assertAll(
                () -> assertNotNull(responses),
                () -> assertEquals(2, responses.size()),
                () -> assertEquals(1L, responses.get(0).getShareProposalId()),
                () -> assertEquals("12345", responses.get(0).getRequesterZipCode()),
                () -> assertEquals("67890", responses.get(0).getRecipientZipCode()),
                () -> assertEquals("첫 번째 공유 요청입니다", responses.get(0).getMessage()),
                () -> assertEquals(ProposalStatus.PENDING, responses.get(0).getStatus())
        );

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).getAllByRecipientIdOrderByCreatedAtDesc(currentUserId);
    }
    @Test
    @DisplayName("요청받은 공유 내역 - 빈 목록 조회")
    void getReceivedShareProposals_EmptyList() {
        // Given
        Long currentUserId = 1L;
        List<ShareInboxResponse> emptyList = List.of();

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(shareProposalRepository.getAllByRecipientIdOrderByCreatedAtDesc(currentUserId))
                .thenReturn(emptyList);

        // When
        List<ShareInboxResponse> responses = shareProposalService.getReceivedShareProposals();

        // Then
        assertAll(
                () -> assertNotNull(responses),
                () -> assertTrue(responses.isEmpty())
        );

        verify(authFacade).getCurrentUserId();
        verify(shareProposalRepository).getAllByRecipientIdOrderByCreatedAtDesc(currentUserId);
    }
}