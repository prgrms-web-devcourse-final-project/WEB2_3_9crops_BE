package io.crops.warmletter.domain.share.service;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.exception.ShareInvalidInputValue;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharePostLikeService {

    private final PostLikeRedisManager postLikeRedisManager;
    private final SharePostLikeRepository sharePostLikeRepository;
    private final AuthFacade authFacade;

    public void toggleLike(Long postId) {
        Long memberId = authFacade.getCurrentUserId();
        postLikeRedisManager.toggleLike(postId, memberId);
    }

    public SharePostLikeResponse getLikeCountAndStatus(Long sharePostId) {

        Long memberId = authFacade.getCurrentUserId();

        if (sharePostId == null)
            throw new ShareInvalidInputValue();

        boolean isLikedInRedis = postLikeRedisManager.isLiked(sharePostId,memberId);

        SharePostLikeResponse response = sharePostLikeRepository.getLikeCountAndStatus(sharePostId,memberId);

        if (isLikedInRedis != response.isLiked()) {
            return new SharePostLikeResponse(response.getLikeCount(), isLikedInRedis);
        }

        return response;
    }
}
