package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.domain.share.repository.ShareProposalLetterRepository;
import io.crops.warmletter.domain.share.repository.ShareProposalRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @InjectMocks
    private ShareProposalService shareProposalService;

    @Test
    @DisplayName("공유 제안 요청 성공")
    void requestShareProposal_Success() {
        // given
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                1L,
                2L,
                "공유 요청"
        );

        ShareProposal shareProposal = request.toEntity();
        ReflectionTestUtils.setField(shareProposal, "id", 1L);

        ShareProposalResponse expectedResponse = ShareProposalResponse.builder().shareProposalId(1L).zipCode("12345").build();
        when(shareProposalRepository.save(any(ShareProposal.class))).thenReturn(shareProposal);
        when(shareProposalRepository.findShareProposalWithZipCode(anyLong())).thenReturn(expectedResponse);

        // when
        ShareProposalResponse response = shareProposalService.requestShareProposal(request);

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(expectedResponse.getShareProposalId(), response.getShareProposalId()),
                () -> assertEquals(expectedResponse.getZipCode(), response.getZipCode())
        );

        verify(shareProposalRepository).save(any(ShareProposal.class));
        verify(shareProposalLetterRepository).saveAll(anyList());
        verify(shareProposalRepository).findShareProposalWithZipCode(anyLong());
    }

    @Test
    @DisplayName("필수값(requesterId) 누락시 예외 발생")
    void requestShareProposal_WithoutRequesterId() {
        // given
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                null,  // requesterId null
                2L,
                "공유 요청"
        );

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> shareProposalService.requestShareProposal(request));

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(shareProposalRepository, never()).save(any(ShareProposal.class));
    }

    @Test
    @DisplayName("필수값(letters) 누락시 예외 발생")
    void requestShareProposal_EmptyLetters() {
        // given
        ShareProposalRequest request = new ShareProposalRequest(
                Collections.emptyList(),  // empty letters
                1L,
                2L,
                "공유 요청"
        );

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> shareProposalService.requestShareProposal(request));

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(shareProposalRepository, never()).save(any(ShareProposal.class));
    }

    @Test
    @DisplayName("Response가 null일 경우 예외 발생")
    void requestShareProposal_ResponseNotFound() {
        // given
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                1L,
                2L,
                "공유 요청"
        );

        ShareProposal shareProposal = request.toEntity();
        ReflectionTestUtils.setField(shareProposal, "id", 1L);

        when(shareProposalRepository.save(any(ShareProposal.class))).thenReturn(shareProposal);
        when(shareProposalRepository.findShareProposalWithZipCode(anyLong())).thenReturn(null);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> shareProposalService.requestShareProposal(request));

        assertEquals(ErrorCode.SHARE_POST_NOT_FOUND, exception.getErrorCode());
        verify(shareProposalRepository).save(any(ShareProposal.class));
        verify(shareProposalLetterRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Letter 저장 실패시 예외 발생")
    void requestShareProposal_LetterSaveFail() {
        // given
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                1L,
                2L,
                "공유 요청"
        );

        ShareProposal shareProposal = request.toEntity();
        ReflectionTestUtils.setField(shareProposal, "id", 1L);

        when(shareProposalRepository.save(any(ShareProposal.class))).thenReturn(shareProposal);
        when(shareProposalLetterRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("저장 실패"));

        // when & then
        assertThrows(RuntimeException.class,
                () -> shareProposalService.requestShareProposal(request));

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
    @DisplayName(" 공유 제안 승인 성공")
    void approveShareProposal_Success() {
        // given
        Long shareProposalId = 1L;

        ShareProposal shareProposal = new ShareProposal(1L, 2L, "test");  // 생성자 사용
        ReflectionTestUtils.setField(shareProposal, "id", shareProposalId);

        SharePost sharePost = SharePost.builder()  // SharePost는 빌더 그대로 사용
                .shareProposalId(shareProposalId)
                .content("test")
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(sharePost, "id", 1L);

        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.of(shareProposal));
        when(sharePostRepository.save(any(SharePost.class)))
                .thenReturn(sharePost);

        // when
        ShareProposalStatusResponse response = shareProposalService.approveShareProposal(shareProposalId);

        // then
        assertThat(response.getShareProposalId()).isEqualTo(shareProposalId);
    }

    @Test
    @DisplayName("공유 제안 거절 성공")
    void rejectShareProposal_Success() {
        // given
        Long shareProposalId = 1L;

        ShareProposal shareProposal = new ShareProposal(1L, 2L, "test");  // 생성자 사용
        ReflectionTestUtils.setField(shareProposal, "id", shareProposalId);

        when(shareProposalRepository.findById(shareProposalId))
                .thenReturn(Optional.of(shareProposal));

        // when
        ShareProposalStatusResponse response = shareProposalService.rejectShareProposal(shareProposalId);

        // then
        assertThat(response.getShareProposalId()).isEqualTo(shareProposalId);
        assertThat(response.getStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(shareProposal.getStatus()).isEqualTo(ProposalStatus.REJECTED);
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
}