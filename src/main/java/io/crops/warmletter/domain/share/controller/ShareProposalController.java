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

    @PostMapping("/share-proposal")
    public ResponseEntity<BaseResponse<ShareProposalResponse>> requestShareProposal(
            @RequestBody ShareProposalRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(shareProposalService.requestShareProposal(request), "요청 완료"));
    }

    //request 체크
    // APPROVED 값시, SharePost 엔티티 생성 필요.
    // 어차피 공유 게시판 조회는 isActive = true 값만 들어감.
    // SharePost 엔티티 isActive = true 값 생성 필요.
    // 이거로 공유 제안 게시판에 대한 상태 관리.
    @PatchMapping("/share-proposal/{shareProposalId}/approve")
    public ResponseEntity<BaseResponse<ShareProposalStatusResponse>> approveShareProposal(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.approveShareProposal(shareProposalId), "공유 요청이 승인되었습니다."));
    }


}