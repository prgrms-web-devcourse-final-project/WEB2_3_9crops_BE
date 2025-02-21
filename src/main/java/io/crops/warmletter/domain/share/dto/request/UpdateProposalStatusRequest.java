package io.crops.warmletter.domain.share.dto.request;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProposalStatusRequest {

    private Long shareProposalId;
    private ProposalStatus status;
    private String content;

}