package io.crops.warmletter.domain.share.dto.response;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ShareProposalDetailResponse {

    private Long shareProposalId;
    private String requesterZipCode;
    private String recipientZipCode;
    private String message;
    private ProposalStatus status;
    private List<ShareLetterPostResponse> letters;
}
