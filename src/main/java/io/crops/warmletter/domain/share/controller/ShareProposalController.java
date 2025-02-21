package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.service.ShareProposalService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShareProposalController {

    private final ShareProposalService shareProposalService;

    @PostMapping("/share-proposals")
    public ResponseEntity<BaseResponse<ShareProposalResponse>> requestShareProposal(
            @RequestBody ShareProposalRequest request)
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(shareProposalService.requestShareProposal(request), "요청 완료"));
    }

}