package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.request.UpdateProposalStatusRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.service.ShareProposalService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShareProposalController {

    private final ShareProposalService shareProposalService;

    @PostMapping("/share-proposals")
    public ResponseEntity<BaseResponse<ShareProposalResponse>> requestShareProposal(
            @RequestBody ShareProposalRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(shareProposalService.requestShareProposal(request), "요청 완료"));
    }

    @PatchMapping("/share-proposal/{shareProposalId}/approve")
    public ResponseEntity<BaseResponse<ShareProposalStatusResponse>> approveShareProposal(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.approveShareProposal(shareProposalId),"공유 요청 성공"));
    }

    @PatchMapping("/share-proposal/{shareProposalId}/reject")
    public ResponseEntity<BaseResponse<ShareProposalStatusResponse>> rejectShareProposal(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.rejectShareProposal(shareProposalId),"공유 요청 거절"));
    }
}