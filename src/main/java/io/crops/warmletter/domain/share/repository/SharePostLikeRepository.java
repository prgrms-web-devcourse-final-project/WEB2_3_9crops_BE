package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.entity.SharePostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SharePostLikeRepository extends JpaRepository<SharePostLike,Long> {

    Optional<SharePostLike> findBySharePostIdAndMemberId(Long sharePostId, Long memberId);
    @Query("SELECT new io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse(" +
            "COUNT(CASE WHEN spl.isLiked = true THEN 1 ELSE NULL END), " +
            "(SELECT s.isLiked FROM SharePostLike s " +
            " WHERE s.sharePostId = :sharePostId AND s.memberId = :memberId)) " +
            "FROM SharePostLike spl " +
            "WHERE spl.sharePostId = :sharePostId")
    SharePostLikeResponse getLikeCountAndStatus(@Param("sharePostId") Long sharePostId,
                                                @Param("memberId") Long memberId);

}
