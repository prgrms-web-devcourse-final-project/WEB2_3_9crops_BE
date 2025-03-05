package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.dto.response.ShareInboxResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;

import java.util.List;

public interface ShareProposalRepositoryCustom {
    ShareProposalResponse findShareProposalWithZipCode(Long shareProposalId);

    List<ShareInboxResponse> getAllByRecipientIdOrderByCreatedAtDesc(Long receiverId);

}
