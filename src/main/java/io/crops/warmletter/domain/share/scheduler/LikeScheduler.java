package io.crops.warmletter.domain.share.scheduler;

import io.crops.warmletter.domain.share.entity.SharePostLike;
import io.crops.warmletter.domain.share.redis.PostLikeRedisManager;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LikeScheduler {

    private static final String REDIS_KEY_DELIMITER = ":";
    private final PostLikeRedisManager postLikeRedisManager;
    private final SharePostLikeRepository sharePostLikeRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncLikesToDatabase() {
        Map<String, Boolean> likeStatusMap = postLikeRedisManager.getAllLikeStatus();

        likeStatusMap.forEach(this::processLikeEntry);

        postLikeRedisManager.clearCache();
    }

    private void processLikeEntry(String key, boolean currentLikeStatus) {
        String[] parts = key.split(REDIS_KEY_DELIMITER);
        Long postId = Long.parseLong(parts[1]);
        Long memberId = Long.parseLong(parts[4]);

        sharePostLikeRepository.findBySharePostIdAndMemberId(postId, memberId)
                .ifPresentOrElse(
                        likeEntity -> updateLike(likeEntity, currentLikeStatus),
                        () -> createLikeIfNeeded(postId, memberId, currentLikeStatus)
                );
    }

    private void updateLike(SharePostLike likeEntity, boolean redisLikeStatus) {
        boolean isSameStatus = likeEntity.isLiked() == redisLikeStatus;
        boolean newStatus = isSameStatus ? !redisLikeStatus : redisLikeStatus;

        likeEntity.updateLikeStatus(newStatus);
    }

    private void createLikeIfNeeded(Long postId, Long memberId, boolean currentLikeStatus) {
        if (currentLikeStatus) {
            sharePostLikeRepository.save(SharePostLike.builder()
                    .sharePostId(postId)
                    .memberId(memberId)
                    .isLiked(true)
                    .build());
        }
    }
}