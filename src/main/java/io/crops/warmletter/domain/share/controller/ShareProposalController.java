package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.request.UpdateProposalStatusRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.service.ShareProposalService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "ShareProposal", description = "편지 공유 요청 관련 API")
public class ShareProposalController {

    private final ShareProposalService shareProposalService;

    @Operation(summary = "편지 공유 요청", description = "새로운 편지 공유 요청을 생성합니다.")
    @PostMapping("/share-proposals")
    public ResponseEntity<BaseResponse<ShareProposalResponse>> requestShareProposal(
            @Valid @RequestBody ShareProposalRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(shareProposalService.requestShareProposal(request), "요청 완료"));
    }

    @Operation(summary = "편지 공유 요청 승인", description = "특정 ID의 편지 공유 요청을 승인합니다.")
    @PatchMapping("/share-proposal/{shareProposalId}/approve")
    public ResponseEntity<BaseResponse<ShareProposalStatusResponse>> approveShareProposal(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.approveShareProposal(shareProposalId),"공유 요청 성공"));
    }

    @Operation(summary = "편지 공유 요청 거절", description = "특정 ID의 편지 공유 요청을 거절합니다.")
    @PatchMapping("/share-proposal/{shareProposalId}/reject")
    public ResponseEntity<BaseResponse<ShareProposalStatusResponse>> rejectShareProposal(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.rejectShareProposal(shareProposalId),"공유 요청 거절"));
    }
}