package io.crops.warmletter.domain.moderation.cache;

import io.crops.warmletter.domain.moderation.repository.ModerationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import org.slf4j.Logger;


@Component
@RequiredArgsConstructor
public class ModerationCacheInitializer {

    private final ModerationRepository moderationRepository;
    private static final Logger log = LoggerFactory.getLogger(ModerationCacheInitializer.class);
    private final RedisTemplate<String, String> redisTemplate;



    @PostConstruct //서버실행될 때 자동실행
    public void loadBannedWordsRedis() {
        List<String> words = moderationRepository.findAllWordsOnly();
        if(!words.isEmpty()) {
            redisTemplate.delete("banned_words");
            redisTemplate.opsForSet().add("banned_words", words.toArray(new String[0]));
        }
        log.info("🚀 금칙어 Redis 로드 완료");
    }

}
