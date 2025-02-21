package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
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

        ShareProposalResponse serviceResponse = ShareProposalResponse.builder()
                .shareProposalId(1L)
                .zipCode("12345")
                .status(ProposalStatus.PENDING.name())
                .build();

        when(shareProposalService.requestShareProposal(any(ShareProposalRequest.class)))
                .thenReturn(serviceResponse);

        // when Controller의 메서드를 호출 후 응답 값이 들어올 때
        ResponseEntity<BaseResponse<ShareProposalResponse>> response =
                shareProposalController.requestShareProposal(request);

        // then 해당 값 검증
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
    void requestShareProposal_ValidationFail() {
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
}