package io.crops.warmletter.domain.badword.cache;

import io.crops.warmletter.domain.badword.dto.response.BadWordResponse;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BadWordCacheInitializerTest {

    @Mock
    private BadWordRepository badWordRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private BadWordCacheInitializer badWordCacheInitializer;

    private static final String BAD_WORD_KEY = "bad_word";

    @BeforeEach
    void setUp() {
        badWordCacheInitializer = new BadWordCacheInitializer(badWordRepository, redisTemplate);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("금칙어가 있을 때 Redis에 정상적으로 로드")
    void loadBadWordsRedis_WithWords() {
        Long id1 = 1L;
        Long id2 = 2L;
        // given
        List<BadWordResponse> badWords = List.of(
                new BadWordResponse(id1, "시발"),
                new BadWordResponse(id2, "병신")
        );
        when(badWordRepository.findAllBadWords()).thenReturn(badWords);


        // when
        badWordCacheInitializer.loadBadWordsRedis();

        // then
        verify(redisTemplate).delete(BAD_WORD_KEY);
        verify(hashOperations).put(BAD_WORD_KEY, id1.toString(), "시발");
        verify(hashOperations).put(BAD_WORD_KEY, id2.toString(), "병신");
    }
}
