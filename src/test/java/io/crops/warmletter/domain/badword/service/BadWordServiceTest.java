package io.crops.warmletter.domain.badword.service;

import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.dto.response.UpdateBadWordResponse;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.exception.BadWordContainsException;
import io.crops.warmletter.domain.badword.exception.BadWordNotFoundException;
import io.crops.warmletter.domain.badword.exception.DuplicateBadWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.*;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertFalse;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class BadWordServiceTest {

    @InjectMocks
    private BadWordService badWordService;

    @Mock
    private BadWordRepository badWordRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private static final String BAD_WORD_KEY = "bad_word";
    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }


    @Test
    @DisplayName("금칙어 저장 성공")
    void saveModerationWord_success() {
        // given
        CreateBadWordRequest request = new CreateBadWordRequest("불량한단어");
        when(badWordRepository.existsByWord("불량한단어")).thenReturn(false);
        when(badWordRepository.save(any(BadWord.class))).thenAnswer(invocation -> {
            BadWord badWord = invocation.getArgument(0);
            // 테스트 코드에서 리플렉션을 이용해 원래 객체의 id 필드를 설정
            ReflectionTestUtils.setField(badWord, "id", 1L);
            return badWord;
        });
        // when
        badWordService.createBadWord(request);

        // then
        verify(badWordRepository).save(any(BadWord.class));
        verify(hashOperations).put("bad_word", "1", "불량한단어");
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
        // when
        badWordService.updateBadWordStatus(1L, request);

        // then
        assertTrue(badWord.isUsed());
        // 서비스 코드에서 hashOperations.put 호출하므로 해당 호출 검증
        verify(hashOperations).put("bad_word", "1", "비속어");
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
        verify(hashOperations).delete("bad_word", "1", "비속어");
    }

    @Test
    @DisplayName("금칙어 포함 시 예외 발생")
    void validateText_containsBadWord_throwsException() {
        // given
        String text = "욕설이 들어간 문장";

        Map<Object, Object> entries = new HashMap<>();
        entries.put("1", "욕설");

        when(hashOperations.entries("bad_word")).thenReturn(entries);


        // when & then
        assertThatThrownBy(() -> badWordService.validateText(text))
                .isInstanceOf(BadWordContainsException.class);
    }



//
    @Test
    @DisplayName("금칙어가 포함되어 있지 않을 때 정상 통과")
    void validateText_noBadWord_success() {
        String inputText = "착한 말";

        Map<Object, Object> entries = new HashMap<>();
        entries.put("1", "비속어");

        lenient().when(hashOperations.entries("bad_word")).thenReturn(entries);
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

    @Test
    @DisplayName("금칙어 조회 성공")
    void getBadWords_Success() {
        Map<Object, Object> entries = new HashMap<>();
        entries.put("1" ,"시발");
        entries.put("2" ,"나쁜욕");
        when(hashOperations.entries(BAD_WORD_KEY)).thenReturn(entries);

        List<Map<String, String>> badWords = badWordService.getBadWords();
        assertEquals(2, badWords.size());
    }

    @Test
    @DisplayName("금칙어 업데이트 성공 - 단어 변경, 캐시 업데이트")
    void updateBadWord_success_withRedisUpdate() {
        // given
        Long id = 1L;
        BadWord existingBadWord = BadWord.builder()
                .id(id)
                .word("oldWord")
                .isUsed(true)
                .build();
        UpdateBadWordRequest request = new UpdateBadWordRequest("newWord");

        when(badWordRepository.findById(id)).thenReturn(Optional.of(existingBadWord));
        when(badWordRepository.existsByWord("newWord")).thenReturn(false);
        // 저장 시, 반환된 객체는 id가 유지되는 것으로 가정
        when(badWordRepository.save(any(BadWord.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        // when
        UpdateBadWordResponse response = badWordService.updateBadWord(id, request);

        // then
        verify(badWordRepository).save(existingBadWord);
        verify(hashOperations).put(BAD_WORD_KEY, id.toString(), "newWord");
        // 응답 확인 (현재 UpdateBadWordResponse는 단어만 반환)
        assertEquals("newWord", response.getWord());
    }

    // 중복 체크: 기존 단어와 다르지만 새 단어가 DB에 존재하는 경우 DuplicateBadWordException 발생
    @Test
    @DisplayName("금칙어 업데이트 실패 - 중복 단어")
    void updateBadWord_failure_duplicate() {
        // given
        Long id = 1L;
        BadWord existingBadWord = BadWord.builder()
                .id(id)
                .word("oldWord")
                .isUsed(true)
                .build();
        UpdateBadWordRequest request = new UpdateBadWordRequest("duplicateWord");

        when(badWordRepository.findById(id)).thenReturn(Optional.of(existingBadWord));
        when(badWordRepository.existsByWord("duplicateWord")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> badWordService.updateBadWord(id, request))
                .isInstanceOf(DuplicateBadWordException.class);
    }

    // 업데이트 성공: 단어 변경, 중복 없음, isUsed가 false인 경우 Redis 캐시 업데이트 없이 DB만 업데이트
    @Test
    @DisplayName("금칙어 업데이트 성공 - 단어 변경, 캐시 미업데이트(isUsed=false)")
    void updateBadWord_success_withoutRedisUpdate() {
        // given
        Long id = 1L;
        BadWord existingBadWord = BadWord.builder()
                .id(id)
                .word("oldWord")
                .isUsed(false)
                .build();
        UpdateBadWordRequest request = new UpdateBadWordRequest("newWord");

        when(badWordRepository.findById(id)).thenReturn(Optional.of(existingBadWord));
        when(badWordRepository.existsByWord("newWord")).thenReturn(false);
        when(badWordRepository.save(any(BadWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UpdateBadWordResponse response = badWordService.updateBadWord(id, request);

        // then
        verify(badWordRepository).save(existingBadWord);
        // Redis 캐시 업데이트가 호출되지 않아야 함
        verify(redisTemplate.opsForHash(), never()).put(anyString(), anyString(), anyString());
        // 응답 확인
        assertEquals("newWord", response.getWord());
    }

}