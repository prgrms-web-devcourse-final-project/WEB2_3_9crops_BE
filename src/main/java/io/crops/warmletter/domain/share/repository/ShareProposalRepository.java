package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.entity.ShareProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShareProposalRepository extends JpaRepository<ShareProposal,Long >, ShareProposalRepositoryCustom {

    Optional<ShareProposal> findByIdAndRecipientId(Long shareProposalId, Long memberId);
}
