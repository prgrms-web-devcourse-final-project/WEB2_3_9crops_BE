package io.crops.warmletter.domain.share.dto.response;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareProposalStatusResponse {
    private Long shareProposalId;
    private ProposalStatus status;
    private Long sharePostId;  // APPROVED 상태일 때만 값이 있음
}
