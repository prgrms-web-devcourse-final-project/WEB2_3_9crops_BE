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

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

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
        verify(redisTemplate).delete("banned_words");
        verify(setOperations).add(eq("banned_words"), eq("시발"), eq("병신"));
    }

    @Test
    @DisplayName("금칙어가 없을 때 Redis에 아무 일도 발생하지 않음")
    void loadBadWordsRedis_WithNoWords() {
        // given
        when(badWordRepository.findAllWordsOnly()).thenReturn(List.of());

        // when
        badWordCacheInitializer.loadBadWordsRedis();

        // then
        verify(redisTemplate, never()).delete(any(String.class));
        verify(setOperations, never()).add(any(), any());
    }

    @Test
    @DisplayName("금칙어가 null일 때 Redis 작업 안 함")
    void loadBadWordsRedis_WithNull() {
        // given
        when(badWordRepository.findAllWordsOnly()).thenReturn(null);

        // when
        badWordCacheInitializer.loadBadWordsRedis();

        // then
        verify(redisTemplate, never()).delete(any(String.class));
        verify(setOperations, never()).add(any(), any());
    }
}
