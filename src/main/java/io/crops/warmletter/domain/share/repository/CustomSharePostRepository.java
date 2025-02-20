package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;

import java.util.Optional;

public interface CustomSharePostRepository {

    Optional<SharePostDetailResponse> findDetailById(Long sharePostId);
}
