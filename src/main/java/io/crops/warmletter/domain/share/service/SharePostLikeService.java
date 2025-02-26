package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.exception.SharePostNotFoundException;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharePostLikeService {

    private final PostLikeRedisManager postLikeRedisManager;

    private final SharePostLikeRepository sharePostLikeRepository;

    public void toggleLike(Long postId, Long memberId) {
        postLikeRedisManager.toggleLike(postId, memberId);
    }

    public SharePostLikeResponse getLikeCountAndStatus(Long sharePostId, Long memberId) {
        if (sharePostId == null)
            throw new SharePostNotFoundException();

        return sharePostLikeRepository.getLikeCountAndStatus(sharePostId, memberId);
    }
}
