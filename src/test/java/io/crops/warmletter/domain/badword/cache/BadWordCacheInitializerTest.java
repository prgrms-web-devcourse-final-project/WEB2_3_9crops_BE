package io.crops.warmletter.domain.badword.cache;

import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
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
    private SetOperations<String, String> setOperations;

    private BadWordCacheInitializer badWordCacheInitializer;

    @BeforeEach
    void setUp() {
        badWordCacheInitializer = new BadWordCacheInitializer(badWordRepository, redisTemplate);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("금칙어가 있을 때 Redis에 정상적으로 로드")
    void loadBadWordsRedis_WithWords() {
        // given
        List<String> words = List.of("시발", "병신");
        when(badWordRepository.findAllWordsOnly()).thenReturn(words);

        // when
        badWordCacheInitializer.loadBadWordsRedis();

        // then
        verify(redisTemplate).delete("bad_word");
        verify(setOperations).add(eq("bad_word"), eq("시발"), eq("병신"));
    }

}
