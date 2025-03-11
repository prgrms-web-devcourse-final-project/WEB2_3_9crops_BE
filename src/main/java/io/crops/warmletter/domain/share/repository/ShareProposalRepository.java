package io.crops.warmletter.domain.share.repository;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareProposalRepository extends JpaRepository<ShareProposal,Long >, ShareProposalRepositoryCustom {
    @Query("SELECT m.zipCode " +
            "FROM ShareProposal proposal " +
            "JOIN Member m ON proposal.requesterId = m.id " +
            "WHERE proposal.id = :id " +
            "AND proposal.requesterId = :requesterId ")
    String findZipCodeByRequesterId(Long id, Long requesterId);

    @Query("SELECT m.zipCode " +
            "FROM ShareProposal proposal " +
            "JOIN Member m ON proposal.recipientId = m.id " +
            "WHERE proposal.id = :id " +
            "AND proposal.recipientId = :recipientId ")
    String findZipCodeByRecipientId(Long id, Long recipientId);
}
