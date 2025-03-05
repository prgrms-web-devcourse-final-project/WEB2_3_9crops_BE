package io.crops.warmletter.domain.share.dto.response;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareInboxResponse {

    private Long shareProposalId;  // 생성된 공유 요청 ID
    private String requesterZipCode;        // 요청자의 우편번호(식별자)
    private String recipientZipCode;
    private String message;
    private ProposalStatus status;
}
