package io.crops.warmletter.domain.badword.service;

import io.crops.warmletter.domain.badword.dto.request.BadWordRequest;
import io.crops.warmletter.domain.badword.exception.DuplicateBannedWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional // 테스트 끝나면 롤백 (DB 안 지저분해짐)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 각 테스트 끝날 때마다 컨텍스트 초기화 (DB 상태 초기화 효과)
class BadWordServiceIntegrationTest {

    @Autowired
    private BadWordService badWordService;

    @Autowired
    private BadWordRepository badWordRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("금칙어 저장 성공")
    void saveModerationWord_success() {
        // given
        BadWordRequest request = new BadWordRequest("십새끼");

        // when
        badWordService.saveModerationWord(request);

        // then
        boolean exists = badWordRepository.existsByWord("십새끼");
        assertTrue(exists);
        Boolean isRedis = redisTemplate.opsForSet().isMember("banned_words", "십새끼");
        assertTrue(isRedis);
    }

    @Test
    @DisplayName("이미 등록된 금칙어일 때 예외 발생")
    void saveModerationWord_duplicate() {
        BadWordRequest request = new BadWordRequest("십새끼");
        badWordService.saveModerationWord(request);

        assertThrows(DuplicateBannedWordException.class, () -> badWordService.saveModerationWord(request));
    }
}