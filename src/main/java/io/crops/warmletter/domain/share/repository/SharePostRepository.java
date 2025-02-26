package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SharePostRepository extends JpaRepository<SharePost,Long>, CustomSharePostRepository {

    Page<SharePost> findAllByIsActiveTrue(Pageable pageable);

    @Query("SELECT new io.crops.warmletter.domain.share.dto.response.SharePostResponse(" +
            "sp.shareProposalId, writer.zipCode, recipient.zipCode, sp.content, sp.isActive, sp.createdAt) " +
            "FROM SharePost sp " +
            "JOIN ShareProposal proposal ON sp.shareProposalId = proposal.id " +
            "JOIN Member writer ON proposal.requesterId = writer.id " +
            "JOIN Member recipient ON proposal.recipientId = recipient.id " +
            "WHERE sp.isActive = true " +
            "ORDER BY sp.createdAt DESC")
    Page<SharePostResponse> findAllActiveSharePostsWithZipCodes(Pageable pageable);
}
