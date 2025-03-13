package io.crops.warmletter.domain.share.scheduler;
import io.crops.warmletter.domain.share.entity.SharePostLike;
import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeScheduler {

    private static final String REDIS_KEY_DELIMITER = ":";
    private final PostLikeRedisManager postLikeRedisManager;
    private final SharePostLikeRepository sharePostLikeRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncLikesToDatabase() {

        try {
            Map<String, Boolean> likeStatusMap = postLikeRedisManager.getAllLikeStatus();
            if (likeStatusMap != null) {
                // 각 좋아요 처리
                for (Map.Entry<String, Boolean> entry : likeStatusMap.entrySet()) {
                    try {
                        processLikeEntry(entry.getKey(), entry.getValue());
                    } catch (Exception e) {
                        log.error("해당 좋아요 항목 처리 중 오류 발생 : {}", entry.getKey(), e);
                    }
                }
            }
                postLikeRedisManager.clearCache();
            } catch(Exception e){
                log.error("DB 동기화 중 오류 발생", e);
            }
        }

    private void processLikeEntry(String key, boolean currentLikeStatus) {
        String[] parts = key.split(REDIS_KEY_DELIMITER);
        // 키 형식 검증
        if (parts.length < 5 || !"post".equals(parts[0]) || !"like".equals(parts[2]) || !"memberId".equals(parts[3])) {
            log.warn("Invalid key format: {}", key);
            return;
        }
        Long postId = Long.parseLong(parts[1]);
        Long memberId = Long.parseLong(parts[4]);

        sharePostLikeRepository.findBySharePostIdAndMemberId(postId, memberId)
                .ifPresentOrElse(
                        likeEntity -> updateLike(likeEntity, currentLikeStatus),
                        () -> createLikeIfNeeded(postId, memberId, currentLikeStatus)
                );
    }

    private void updateLike(SharePostLike likeEntity, boolean redisLikeStatus) {
        if (likeEntity.isLiked() != redisLikeStatus) {
            likeEntity.updateLikeStatus(redisLikeStatus);
        }
    }

    //
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