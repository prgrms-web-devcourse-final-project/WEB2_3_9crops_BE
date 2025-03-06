package io.crops.warmletter.domain.share.repository;

import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;

import java.util.List;
import java.util.Optional;

public interface CustomSharePostRepository {

    Optional<SharePostDetailResponse> findDetailById(Long sharePostId);

    List<SharePostResponse> findMyRequestedActiveSharePosts(Long memberId);

}
