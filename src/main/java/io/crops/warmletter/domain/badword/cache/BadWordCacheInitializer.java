package io.crops.warmletter.domain.badword.cache;

import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import org.slf4j.Logger;


@Component
@RequiredArgsConstructor
public class BadWordCacheInitializer {

    private final BadWordRepository badWordRepository;
    private static final Logger log = LoggerFactory.getLogger(BadWordCacheInitializer.class);
    private final RedisTemplate<String, String> redisTemplate;



    @PostConstruct //서버실행될 때 자동실행
    public void loadBadWordsRedis() {
        List<String> words = badWordRepository.findAllWordsOnly();
        if(!words.isEmpty()) {
            redisTemplate.delete("bad_word");
            redisTemplate.opsForSet().add("bad_word", words.toArray(new String[0]));
        }
        log.info("🚀 금칙어 Redis 로드 완료");
    }

}
