package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.entity.SharePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareRepository extends JpaRepository<SharePost,Long> {
}
