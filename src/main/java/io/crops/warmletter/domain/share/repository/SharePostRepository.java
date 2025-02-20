package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.entity.SharePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharePostRepository extends JpaRepository<SharePost,Long>, CustomSharePostRepository {

    Page<SharePost> findAllByIsActiveTrue(Pageable pageable);

}
