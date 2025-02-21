package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;

public interface ShareProposalRepositoryCustom {
    ShareProposalResponse findShareProposalWithZipCode(Long shareProposalId);
}
