package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.entity.SharePostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SharePostLikeRepository extends JpaRepository<SharePostLike,Long> {

    Optional<SharePostLike> findBySharePostIdAndMemberId(Long sharePostId, Long memberId);


}
