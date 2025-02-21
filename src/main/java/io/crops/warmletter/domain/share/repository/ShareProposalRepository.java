package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.entity.ShareProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareProposalRepository extends JpaRepository<ShareProposal,Long >, ShareProposalRepositoryCustom {

}
