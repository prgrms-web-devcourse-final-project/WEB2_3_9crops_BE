package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharePostLikeService {

    private final PostLikeRedisManager postLikeRedisManager;

    public void toggleLike(Long postId, Long memberId) {
        postLikeRedisManager.toggleLike(postId, memberId);
    }

}
