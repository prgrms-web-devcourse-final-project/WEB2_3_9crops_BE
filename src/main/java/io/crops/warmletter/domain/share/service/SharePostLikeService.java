package io.crops.warmletter.domain.share.service;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.exception.ShareInvalidInputValue;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SharePostLikeService {

    private final PostLikeRedisManager postLikeRedisManager;
    private final SharePostLikeRepository sharePostLikeRepository;
    private final AuthFacade authFacade;

    public void toggleLike(Long postId) {
        Long memberId = authFacade.getCurrentUserId();
        // Redis 먼저 조회 후, DB 조회
        Optional<Boolean> redisLikeStatus = postLikeRedisManager.getLikedStatus(postId, memberId);

        boolean currentLikeStatus = redisLikeStatus.orElseGet(() ->
                sharePostLikeRepository.findBySharePostIdAndMemberId(postId, memberId)
                        .map(entity -> entity.isLiked())
                        .orElse(false)
        );

        boolean newStatus = !currentLikeStatus;

        postLikeRedisManager.toggleLike(postId, memberId, newStatus);
    }

    public SharePostLikeResponse getLikeCountAndStatus(Long sharePostId) {

        Long memberId = authFacade.getCurrentUserId();

        if (sharePostId == null)
            throw new ShareInvalidInputValue();
        // DB 조회
        SharePostLikeResponse dbResponse = sharePostLikeRepository.getLikeCountAndStatus(sharePostId, memberId);
        // Redis에서 동기화안된 카운트 가져옴.
        int redisLikeCount = postLikeRedisManager.getLikeCount(sharePostId);
        // 좋아요 상태 확인 없으면 DB 값으로
        Optional<Boolean> redisLikeStatus = postLikeRedisManager.getLikedStatus(sharePostId, memberId);
        boolean isLiked = redisLikeStatus.orElse(dbResponse.isLiked());

        return new SharePostLikeResponse(
                dbResponse.getLikeCount() + redisLikeCount,
                isLiked
        );

    }
}
