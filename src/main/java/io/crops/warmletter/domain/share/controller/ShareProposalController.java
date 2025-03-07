package io.crops.warmletter.domain.share.controller;
import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareInboxResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalDetailResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/share-proposals")
@RequiredArgsConstructor
@Tag(name = "편지 공유 API", description = "편지 공유 요청,수락,거절,조회 관련 API")
public class ShareProposalController {

    private final ShareProposalService shareProposalService;

    @Operation(summary = "편지 공유 요청", description = "새로운 편지 공유 요청을 생성합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<ShareProposalResponse>> requestShareProposal(
            @Valid @RequestBody ShareProposalRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(shareProposalService.requestShareProposal(request), "공유 요청 완료"));
    }

    @Operation(summary = "편지 공유 요청 승인", description = "특정 ID의 편지 공유 요청을 승인합니다.")
    @PatchMapping("/{shareProposalId}/approve")
    public ResponseEntity<BaseResponse<ShareProposalStatusResponse>> approveShareProposal(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.approveShareProposal(shareProposalId), "공유 요청 승인 성공"));
    }

    @Operation(summary = "편지 공유 요청 거절", description = "특정 ID의 편지 공유 요청을 거절합니다.")
    @PatchMapping("/{shareProposalId}/reject")
    public ResponseEntity<BaseResponse<ShareProposalStatusResponse>> rejectShareProposal(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.rejectShareProposal(shareProposalId), "공유 요청 거절 성공"));
    }
    
    @Operation(summary = "마이페이지 공유 조회", description = " 요청받은 공유 내역을 조회합니다. ")
    @GetMapping("/inbox")
    public ResponseEntity<BaseResponse<List<ShareInboxResponse>>> getReceivedShareProposals() {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.getReceivedShareProposals(),"요청받은 공유 내역 조회 성공"));
    }

    @Operation(summary = "공유 요청 상세 조회", description = "특정 ID의 공유 요청 상세 내역을 조회합니다.")
    @GetMapping("/{shareProposalId}")
    public ResponseEntity<BaseResponse<ShareProposalDetailResponse>> getShareProposalDetail(
            @PathVariable(name = "shareProposalId") Long shareProposalId) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(shareProposalService.getShareProposalDetail(shareProposalId), "공유 요청 상세 조회 성공"));
    }
}