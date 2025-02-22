package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.domain.share.service.ShareProposalService;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import io.crops.warmletter.global.response.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareProposalControllerTest {

    @Mock
    private ShareProposalService shareProposalService;

    @InjectMocks
    private ShareProposalController shareProposalController;

    @Test
    @DisplayName("공유 제안 요청 성공")
    void requestShareProposal_Success() {
        // Given
        ShareProposalRequest request = new ShareProposalRequest(List.of(1L, 2L)
                , 1L,
                2L,
                "공유 요청"
        );

        // Controller 테스트에서 서비스를 mocking할 때 필요한 모든 값을 설정
        ShareProposalResponse serviceResponse = ShareProposalResponse.builder()
                .shareProposalId(1L)
                .zipCode("12345")
                .status(ProposalStatus.PENDING)  // 이 값이 누락되어 있었음
                .build();

        when(shareProposalService.requestShareProposal(any(ShareProposalRequest.class)))
                .thenReturn(serviceResponse);

        // when
        ResponseEntity<BaseResponse<ShareProposalResponse>> response =
                shareProposalController.requestShareProposal(request);

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("요청 완료", response.getBody().getMessage()),
                () -> assertEquals(1L, response.getBody().getData().getShareProposalId()),
                () -> assertEquals("12345", response.getBody().getData().getZipCode()),
                () -> assertEquals(ProposalStatus.PENDING, response.getBody().getData().getStatus())
        );

        verify(shareProposalService).requestShareProposal(request);
    }

    @Test
    @DisplayName("필수 파라미터 누락시 예외 발생")
    void requestShareProposal_Failure() {
        // Given
        ShareProposalRequest request = new ShareProposalRequest(
                List.of(1L, 2L),
                null,
                2L,
                "공유 요청"
        );

        when(shareProposalService.requestShareProposal(any(ShareProposalRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        // When & Then
        assertThrows(BusinessException.class, () ->
                shareProposalController.requestShareProposal(request));

        verify(shareProposalService).requestShareProposal(request);
    }

    @Test
    @DisplayName("공유 요청 승인 - 성공")
    void approveShareProposal_Success() {
        // given
        Long shareProposalId = 1L;
        ShareProposalStatusResponse serviceResponse = ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposalId)
                .status(ProposalStatus.APPROVED)
                .sharePostId(1L)
                .build();

        when(shareProposalService.approveShareProposal(shareProposalId))
                .thenReturn(serviceResponse);

        // when
        ResponseEntity<BaseResponse<ShareProposalStatusResponse>> response =
                shareProposalController.approveShareProposal(shareProposalId);

        // then
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(serviceResponse, response.getBody().getData()),
                () -> assertEquals("공유 요청 성공", response.getBody().getMessage()),
                () -> verify(shareProposalService).approveShareProposal(shareProposalId)
        );
    }

    @Test
    @DisplayName("공유 요청 승인 - 실패")
    void approveShareProposal_Fail() {
        // given
        Long shareProposalId = 1L;
        when(shareProposalService.approveShareProposal(shareProposalId))
                .thenThrow(new BusinessException(ErrorCode.SHARE_PROPOSAL_NOTFOUND));

        // when & then
        assertThrows(BusinessException.class,
                () -> shareProposalController.approveShareProposal(shareProposalId));
        verify(shareProposalService).approveShareProposal(shareProposalId);
    }

    @Test
    @DisplayName("공유 요청 승인 - 존재하지 않는 요청시 예외가 발생한다")
    void approveShareProposal_NotFound() {
        // given
        Long shareProposalId = 999L;
        when(shareProposalService.approveShareProposal(shareProposalId))
                .thenThrow(new BusinessException(ErrorCode.SHARE_PROPOSAL_NOTFOUND));

        // when & then
        assertThrows(BusinessException.class,
                () -> shareProposalController.approveShareProposal(shareProposalId),
                "존재하지 않는 공유 요청에 대해 BusinessException이 발생해야 합니다"
        );
    }

    private ShareProposalStatusResponse createMockResponse(Long shareProposalId) {
        return ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposalId)
                .status(ProposalStatus.APPROVED)
                .sharePostId(1L)
                .build();
    }

    @Test
    @DisplayName("공유 요청 거절 - 성공")
    void rejectShareProposal_Success() {
        // given
        Long shareProposalId = 1L;
        ShareProposalStatusResponse serviceResponse = ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposalId)
                .status(ProposalStatus.REJECTED)
                .build();

        when(shareProposalService.rejectShareProposal(shareProposalId))
                .thenReturn(serviceResponse);

        // when
        ResponseEntity<BaseResponse<ShareProposalStatusResponse>> response =
                shareProposalController.rejectShareProposal(shareProposalId);

        // then
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(serviceResponse, response.getBody().getData()),
                () -> assertEquals("공유 요청 거절", response.getBody().getMessage()),
                () -> verify(shareProposalService).rejectShareProposal(shareProposalId)
        );
    }

    @Test
    @DisplayName("공유 요청 거절 - 실패")
    void rejectShareProposal_Fail() {
        // given
        Long shareProposalId = 1L;
        when(shareProposalService.rejectShareProposal(shareProposalId))
                .thenThrow(new BusinessException(ErrorCode.SHARE_PROPOSAL_NOTFOUND));

        // when & then
        assertThrows(BusinessException.class,
                () -> shareProposalController.rejectShareProposal(shareProposalId));
        verify(shareProposalService).rejectShareProposal(shareProposalId);
    }
}