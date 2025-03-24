package io.crops.warmletter.domain.share.repository;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SharePostRepository extends JpaRepository<SharePost,Long>, CustomSharePostRepository {

    @Query("SELECT new io.crops.warmletter.domain.share.dto.response.SharePostResponse(" +
            "sp.id, sp.shareProposalId, writer.zipCode, recipient.zipCode, sp.content, sp.isActive, sp.createdAt) " +
            "FROM SharePost sp " +
            "JOIN ShareProposal proposal ON sp.shareProposalId = proposal.id " +
            "JOIN Member writer ON proposal.requesterId = writer.id " +
            "JOIN Member recipient ON proposal.recipientId = recipient.id " +
            "WHERE sp.isActive = true " +
            "AND (:cursorId IS NULL OR sp.id < :cursorId) " +
            "ORDER BY sp.id DESC " +
            "LIMIT :size")
    List<SharePostResponse> findAllActiveSharePostsWithZipCodes(@Param("cursorId") Long cursorId, @Param("size") int size);

    @Query("SELECT sp FROM SharePost sp " +
            "JOIN ShareProposal proposal ON sp.shareProposalId = proposal.id " +
            "WHERE sp.id = :sharePostId AND proposal.requesterId = :memberId")
    Optional<SharePost> findByIdAndRequesterId(Long sharePostId, Long memberId);
}
