package io.crops.warmletter.domain.share.dto.response;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareProposalResponse {
    private Long shareProposalId;  // 생성된 공유 요청 ID
    private String zipCode;        // 요청자의 우편번호(식별자)
    private ProposalStatus status;

    @Builder
    public ShareProposalResponse(Long shareProposalId, String zipCode,String status) {
        this.shareProposalId = shareProposalId;
        this.zipCode = zipCode;
        this.status = ProposalStatus.PENDING;
    }

    public ShareProposalResponse(Long shareProposalId, String zipCode) {
        this.shareProposalId = shareProposalId;
        this.zipCode = zipCode;
        this.status = ProposalStatus.PENDING;  // 기본값 설정
    }
}