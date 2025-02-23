package io.crops.warmletter.domain.share.dto.response;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareProposalResponse {
    private Long shareProposalId;  // 생성된 공유 요청 ID
    private String zipCode;        // 요청자의 우편번호(식별자)
    private ProposalStatus status;
}