package io.crops.warmletter.domain.badword.cache;

import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.slf4j.Logger;


@Component
@RequiredArgsConstructor
@Slf4j
public class BadWordCacheInitializer {

    private final BadWordRepository badWordRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BAD_WORD_KEY = "bad_word";


    @PostConstruct //서버실행될 때 자동실행
    public void loadBadWordsRedis() {
        List<Object[]> badWords = badWordRepository.findAllWordsOnly();
        if(!badWords.isEmpty()) {
            redisTemplate.delete(BAD_WORD_KEY);
            for (Object[] row : badWords) {
                String id = row[0].toString();
                String word = row[1].toString();
                redisTemplate.opsForHash().put(BAD_WORD_KEY, id, word);
            }
        }
        log.info("🚀 금칙어 Redis 로드 완료");
    }

}
