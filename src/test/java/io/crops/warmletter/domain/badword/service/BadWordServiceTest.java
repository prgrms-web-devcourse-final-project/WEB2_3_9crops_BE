package io.crops.warmletter.domain.badword.service;

import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.exception.BadWordContainsException;
import io.crops.warmletter.domain.badword.exception.BadWordNotFoundException;
import io.crops.warmletter.domain.badword.exception.DuplicateBadWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertFalse;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Import(TestConfig.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 각 테스트 끝날 때마다 컨텍스트 초기화 (DB 상태 초기화 효과)
class BadWordServiceTest {

    @InjectMocks
    private BadWordService badWordService;

    @Mock
    private BadWordRepository badWordRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @BeforeEach
    void setUp() {
        // RedisTemplate의 opsForSet() 호출 시 미리 준비한 setOperations 반환하도록 설정
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("금칙어 저장 성공")
    void saveModerationWord_success() {
        // given
        CreateBadWordRequest request = new CreateBadWordRequest("십새끼");
        when(badWordRepository.existsByWord("십새끼")).thenReturn(false);
        when(setOperations.isMember("bad_word", "십새끼")).thenReturn(false);

        // when
        badWordService.createBadWord(request);

        // then
        verify(badWordRepository).save(any(BadWord.class));  // save 메서드 호출 검증
        verify(setOperations).add("bad_word", "십새끼");    // Redis add 메서드 호출 검증
    }

    @Test
    @DisplayName("이미 등록된 금칙어일 때 예외 발생")
    void saveModerationWord_duplicate() {
        // given
        CreateBadWordRequest request = new CreateBadWordRequest("십새끼");

        when(badWordRepository.existsByWord("십새끼")).thenReturn(true); // 이미 존재한다고 가정

        // when & then
        assertThatThrownBy(() -> badWordService.createBadWord(request))
                .isInstanceOf(DuplicateBadWordException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_BANNED_WORD.getMessage());

        // 추가적으로 검증해볼 수도 있음 (save 호출 안 됐는지)
        verify(badWordRepository, never()).save(any(BadWord.class));
        verify(setOperations, never()).add(anyString(), anyString());
    }


    @Test
    @DisplayName("금칙어 상태 업데이트 - 활성화 성공")
    void updateBadWordStatus_activate_success() {
        // given
        BadWord badWord = BadWord.builder()
                .word("비속어")
                .isUsed(false)
                .build();
        UpdateBadWordStatusRequest request = new UpdateBadWordStatusRequest(true);

        when(badWordRepository.findById(1L)).thenReturn(Optional.of(badWord));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember("bad_word", "비속어")).thenReturn(false);

        // when
        badWordService.updateBadWordStatus(1L, request);

        // then
        assertTrue(badWord.isUsed());
        verify(setOperations).add("bad_word", "비속어");
    }


    @Test
    @DisplayName("금칙어 상태 업데이트 - 비활성화 성공")
    void updateBadWordStatus_deactivate_success() {
        // given
        BadWord badWord = BadWord.builder()
                .id(1L)  // ID를 직접 할당
                .word("비속어")
                .isUsed(true)
                .build();

        UpdateBadWordStatusRequest request = new UpdateBadWordStatusRequest(false);

        // Mock 동작 설정
        when(badWordRepository.findById(1L)).thenReturn(Optional.of(badWord));
        // when
        badWordService.updateBadWordStatus(1L, request);

        // then
        assertFalse(badWord.isUsed()); // 상태 업데이트 확인
        verify(setOperations).remove("bad_word", "비속어"); // Redis 제거 호출 확인
    }

    @Test
    @DisplayName("금칙어 포함 시 예외 발생")
    void validateText_containsBadWord_throwsException() {
        // given
        String text = "욕설이 들어간 문장";

        Set<String> badWords = new HashSet<>();
        badWords.add("욕설");

        when(setOperations.members("bad_word")).thenReturn(badWords);

        // when & then
        assertThatThrownBy(() -> badWordService.validateText(text))
                .isInstanceOf(BadWordContainsException.class);
    }



//
    @Test
    @DisplayName("금칙어가 포함되어 있지 않을 때 정상 통과")
    void validateText_noBadWord_success() {
        String inputText = "착한 말";

        Set<String> badWords = new HashSet<>();
        badWords.add("비속어");

        when(setOperations.members("bad_word")).thenReturn(badWords);
        assertDoesNotThrow(() -> badWordService.validateText(inputText));
    }

    @Test
    @DisplayName("존재하지 않는 금칙어 ID로 상태 업데이트 시 예외 발생")
    void updateBadWordStatus_notFound_throwsException() {
        // given
        UpdateBadWordStatusRequest request = new UpdateBadWordStatusRequest(true);

        when(badWordRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> badWordService.updateBadWordStatus(999L, request))
                .isInstanceOf(BadWordNotFoundException.class);
    }


}