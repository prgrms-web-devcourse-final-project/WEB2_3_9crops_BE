package io.crops.warmletter.domain.share.controller;
import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareInboxResponse;
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
                "공유 요청"
        );

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

    @Test
    @DisplayName("마이페이지 공유 요청받은 내역 조회 성공")
    void getReceivedShareProposals_Success() {
        // Given
        List<ShareInboxResponse> serviceResponses = List.of(
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

        when(shareProposalService.getReceivedShareProposals())
                .thenReturn(serviceResponses);

        // When
        ResponseEntity<BaseResponse<List<ShareInboxResponse>>> response =
                shareProposalController.getReceivedShareProposals();

        // Then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("요청받은 공유 내역 조회 성공", response.getBody().getMessage()),
                () -> assertEquals(2, response.getBody().getData().size()),
                () -> assertEquals(1L, response.getBody().getData().get(0).getShareProposalId()),
                () -> assertEquals("12345", response.getBody().getData().get(0).getRequesterZipCode()),
                () -> assertEquals("67890", response.getBody().getData().get(0).getRecipientZipCode()),
                () -> assertEquals("첫 번째 공유 요청입니다", response.getBody().getData().get(0).getMessage()),
                () -> assertEquals(ProposalStatus.PENDING, response.getBody().getData().get(0).getStatus()),
                () -> assertEquals(2L, response.getBody().getData().get(1).getShareProposalId()),
                () -> assertEquals(ProposalStatus.APPROVED, response.getBody().getData().get(1).getStatus())
        );

        verify(shareProposalService).getReceivedShareProposals();
    }

    @Test
    @DisplayName("마이페이지 공유 요청받은 내역 - 빈 목록 조회 성공")
    void getReceivedShareProposals_EmptyList() {
        // Given
        List<ShareInboxResponse> emptyList = List.of();
        when(shareProposalService.getReceivedShareProposals())
                .thenReturn(emptyList);

        // When
        ResponseEntity<BaseResponse<List<ShareInboxResponse>>> response =
                shareProposalController.getReceivedShareProposals();

        // Then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("요청받은 공유 내역 조회 성공", response.getBody().getMessage()),
                () -> assertEquals(0, response.getBody().getData().size())
        );

        verify(shareProposalService).getReceivedShareProposals();
    }
}