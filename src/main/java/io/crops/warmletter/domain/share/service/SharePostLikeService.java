package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.request.SharePostLikeRequest;
import io.crops.warmletter.domain.share.redis.PostLikeRedisManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharePostLikeService {

    private final PostLikeRedisManager postLikeRedisManager;

    public void addLike(SharePostLikeRequest request) {
        postLikeRedisManager.addLike(request.getSharePostId(), request.getMemberId());
    }


}
