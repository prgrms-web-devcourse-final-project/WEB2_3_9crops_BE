package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.exception.TemporaryMatchingNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.repository.LetterTemporaryMatchingRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LetterMatchingServiceTest {

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthFacade authFacade;

    @Mock
    private LetterTemporaryMatchingRepository letterTemporaryMatchingRepository;

    @InjectMocks
    private RandomLetterService letterMatchingService;


    @Test
    @DisplayName("랜덤 편지 리스트 확인 - 카테고리가 있을 경우")
    void find_RandomLetters_WithCategory() {
        Category category = Category.CELEBRATION;

        // 10번 회원이 있고
        Member member = Member.builder()
                .zipCode("12345")
                .build();
        ReflectionTestUtils.setField(member, "id", 10L);

        // 10번 회원이 쓴 편지
        Letter letter = Letter.builder()
                .content("내용입니닷")
                .category(Category.CONSOLATION)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .writerId(10L)
                .build();

        RandomLetterResponse dto = RandomLetterResponse.builder()
                .letterId(1L)
                .content("내용입니닷")
                .zipCode("12345")
                .category(Category.CONSOLATION)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .createdAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        //조회 시 편지
        when(letterRepository.findRandomLettersByCategory(category, pageable)).thenReturn(List.of(dto));

        //when
        List<RandomLetterResponse> responses = letterMatchingService.findRandomLetters(category);
        RandomLetterResponse response = responses.get(0);
        // Then
        assertAll("랜덤 편지 응답 검증",
                () -> assertEquals("내용입니닷", response.getContent()),
                () -> assertEquals("12345", response.getZipCode()),
                () -> assertEquals(Category.CONSOLATION, response.getCategory()),
                () -> assertEquals(PaperType.BASIC, response.getPaperType()),
                () -> assertEquals(FontType.KYOBO, response.getFontType())
        );
    }

    @Test
    @DisplayName("findRandomLetters - 카테고리 null 시 전체 랜덤 편지 5개 조회 성공")
    void find_RandomLetters_WithNullCategory() {
        Category category = null;

        // 10번 회원이 있고
        Member member = Member.builder()
                .zipCode("12345")
                .build();
        ReflectionTestUtils.setField(member, "id", 10L);

        // 10번 회원이 쓴 편지
        Letter letter = Letter.builder()
                .content("내용입니닷")
                .category(Category.ETC)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .writerId(10L)
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        RandomLetterResponse dto = RandomLetterResponse.builder()
                .letterId(1L)
                .content("내용입니닷")
                .zipCode("12345")
                .category(Category.ETC)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .createdAt(LocalDateTime.now())
                .build();

        //조회 시 편지
        when(letterRepository.findRandomLettersByCategory(null, pageable)).thenReturn(List.of(dto));

        List<RandomLetterResponse> responses = letterMatchingService.findRandomLetters(category);
        RandomLetterResponse response = responses.get(0);

        assertAll("랜덤 편지 응답 검증",
                () -> assertEquals("내용입니닷", response.getContent()),
                () -> assertEquals("12345", response.getZipCode()),
                () -> assertEquals(Category.ETC, response.getCategory()),
                () -> assertEquals(PaperType.BASIC, response.getPaperType()),
                () -> assertEquals(FontType.KYOBO, response.getFontType())
        );
    }

    @Test
    @DisplayName("checkLastMatched - 마지막 매칭 시간이 null인 경우 (편지 전송 가능)")
    void checkLastMatched_whenNull() {
        Long userId = 1L;
        Member member = Member.builder()
                .lastMatchedAt(null)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(userId);
        when(memberRepository.findById(userId)).thenReturn(Optional.ofNullable(member));

        CheckLastMatchResponse response = letterMatchingService.checkLastMatched();
        assertTrue(response.isCanSend());
        assertNull(response.getLastMatchedAt());
    }

    @Test
    @DisplayName("checkLastMatched - 마지막 매칭 시간이 1시간 이전인 경우 (전송 가능)")
    void checkLastMatched_pastTime() {
        Long userId = 1L;
        LocalDateTime pastTime = LocalDateTime.now().minusHours(2);
        Member member = Member.builder()
                .lastMatchedAt(pastTime)
                .build();
        when(authFacade.getCurrentUserId()).thenReturn(userId);
        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));

        CheckLastMatchResponse response = letterMatchingService.checkLastMatched();
        assertTrue(response.isCanSend());
    }

    @Test
    @DisplayName("checkLastMatched - 마지막 매칭 시간이 1시간 이내인 경우 (전송 불가능)")
    void checkLastMatched_lastMatchedAt() {
        Long userId = 1L;
        LocalDateTime recentTime = LocalDateTime.now().plusMinutes(30);
        Member member = Member.builder()
                .lastMatchedAt(recentTime)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(userId);
        when(memberRepository.findById(userId)).thenReturn(Optional.ofNullable(member));

        CheckLastMatchResponse response = letterMatchingService.checkLastMatched();
        assertFalse(response.isCanSend());
    }

    @Test
    @DisplayName("checkTemporaryMatchedTable - 임시 매칭 데이터가 존재하는 경우")
    void checkTemporaryMatchedTable_exists() {
        Long userId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(userId);
        when(authFacade.getZipCode()).thenReturn("12345");

        // 임시 매칭 데이터 생성
        LetterTemporaryMatching tempMatching = LetterTemporaryMatching.builder()
                .letterId(100L)
                .firstMemberId(2L)
                .secondMemberId(userId)
                .build();
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId)).thenReturn(Optional.ofNullable(tempMatching));

        // Letter 생성 (임시 매칭에 해당하는 편지)
        Letter letter = Letter.builder()
                .title("테스트 편지")
                .content("테스트 내용")
                .category(Category.CONSOLATION)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .writerId(2L)
                .build();

        when(letterRepository.findById(100L)).thenReturn(Optional.of(letter));

        TemporaryMatchingResponse response = letterMatchingService.checkTemporaryMatchedTable();

        assertTrue(response.isTemporary());
        assertEquals("테스트 편지", response.getLetterTitle());
        assertEquals("테스트 내용", response.getContent());
    }

    @Test
    @DisplayName("checkTemporaryMatchedTable - 임시 매칭 데이터가 없는 경우")
    void checkTemporaryMatchedTable_notExists() {
        Long userId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(1L);
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId)).thenReturn(Optional.empty());

        TemporaryMatchingResponse response = letterMatchingService.checkTemporaryMatchedTable();

        assertFalse(response.isTemporary());
    }

    @Test
    @DisplayName("matchingCancel - 임시 매칭 데이터가 존재하는 경우 삭제")
    void matchingCancel_whenExists() {
        Long userId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(userId);
        LetterTemporaryMatching tempMatching = LetterTemporaryMatching.builder()
                .letterId(100L)
                .firstMemberId(2L)
                .secondMemberId(userId)
                .build();
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId)).thenReturn(Optional.of(tempMatching));

        letterMatchingService.matchingCancel();

        verify(letterTemporaryMatchingRepository, times(1)).delete(tempMatching);
    }

    @Test
    @DisplayName("matchingCancel - 임시 매칭 데이터가 존재하지 않는 경우 에러")
    void matchingCancel_notexists() {
        Long userId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(userId);
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId)).thenReturn(Optional.empty());
        assertThrows(TemporaryMatchingNotFoundException.class, () -> letterMatchingService.matchingCancel());
    }

}