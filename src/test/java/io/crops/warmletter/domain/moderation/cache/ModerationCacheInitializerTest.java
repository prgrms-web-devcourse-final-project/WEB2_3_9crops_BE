package io.crops.warmletter.domain.moderation.cache;

import io.crops.warmletter.domain.moderation.repository.ModerationRepository;
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
class ModerationCacheInitializerTest {

    @Mock
    private ModerationRepository moderationRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private ModerationCacheInitializer moderationCacheInitializer;

    @BeforeEach
    void setUp() {
        moderationCacheInitializer = new ModerationCacheInitializer(moderationRepository, redisTemplate);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("금칙어가 있을 때 Redis에 정상적으로 로드")
    void loadBannedWordsRedis_hasWords() {
        // given
        List<String> words = List.of("시발", "병신");
        when(moderationRepository.findAllWordsOnly()).thenReturn(words);

        // when
        moderationCacheInitializer.loadBannedWordsRedis();

        // then
        verify(redisTemplate).delete("banned_words");
        verify(setOperations).add(eq("banned_words"), eq("시발"), eq("병신"));

    }


    @Test
    @DisplayName("금칙어가 없을 때 Redis에 아무 일도 발생하지 않음")
    void loadBannedWordsRedis_noWords() {
        // given
        when(moderationRepository.findAllWordsOnly()).thenReturn(List.of());

        // when
        moderationCacheInitializer.loadBannedWordsRedis();

        // then
        verify(redisTemplate, never()).delete(any(String.class));
        verify(setOperations, never()).add(any(), any());
    }


    @Test
    @DisplayName("금칙어가 null일 때 Redis 작업 안 함")
    void loadBannedWordsRedis_nullWords() {
        // given
        when(moderationRepository.findAllWordsOnly()).thenReturn(Collections.emptyList());


        // when
        moderationCacheInitializer.loadBannedWordsRedis();

        // then
        verify(redisTemplate, never()).delete(any(String.class));

        verify(setOperations, never()).add(any(), any());
    }

}
