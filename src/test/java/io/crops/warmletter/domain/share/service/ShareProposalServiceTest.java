package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.entity.ShareProposal;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareProposalServiceTest {

    @Mock
    private ShareProposalRepository shareProposalRepository;

    @Mock
    private ShareProposalLetterRepository shareProposalLetterRepository;

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

        ShareProposalResponse expectedResponse = new ShareProposalResponse(1L, "12345");

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



}