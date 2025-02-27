package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.request.ApproveLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.exception.AlreadyApprovedException;
import io.crops.warmletter.domain.letter.exception.DuplicateLetterMatchException;
import io.crops.warmletter.domain.letter.exception.TemporaryMatchingNotFoundException;
import io.crops.warmletter.domain.letter.facade.LetterFacade;
import io.crops.warmletter.domain.letter.repository.LetterMatchingRepository;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.repository.LetterTemporaryMatchingRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RandomLetterServiceTest {

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthFacade authFacade;

    @Mock
    private LetterTemporaryMatchingRepository letterTemporaryMatchingRepository;

    @Mock
    private LetterMatchingRepository letterMatchingRepository;

    @Mock
    private LetterFacade letterFacade;

    @InjectMocks
    private RandomLetterService randomLetterService;


    @Test
    @DisplayName("랜덤 편지 리스트 확인 - 카테고리가 있을 경우")
    void find_RandomLetters_WithCategory() {
        Category category = Category.CELEBRATION;

        // 10번 회원이 있고
        Member member = Member.builder()
                .zipCode("12345")
                .preferredLetterCategory(Category.CONSULT)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        // 10번 회원이 쓴 편지
        Letter letter = Letter.builder()
                .title("제목입니다")
                .content("내용입니닷")
                .category(Category.CONSOLATION)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .letterType(LetterType.RANDOM)
                .writerId(10L)
                .build();

        RandomLetterResponse dto = RandomLetterResponse.builder()
                .letterId(1L)
                .writerId(2L)
                .title("제목입니다")
                .zipCode("12345")
                .category(Category.CONSOLATION)
                .createdAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        //조회 시 편지
        when(authFacade.getCurrentUserId()).thenReturn(1L);
        when(letterRepository.findRandomLettersByCategory(category,1L, pageable)).thenReturn(List.of(dto));
        when((memberRepository.findById(1L))).thenReturn(Optional.of(member));

        //when
        List<RandomLetterResponse> responses = randomLetterService.findRandomLetters(category);
        RandomLetterResponse response = responses.get(0);
        // Then
        assertAll("랜덤 편지 응답 검증",
                () -> assertEquals("제목입니다", response.getTitle()),
                () -> assertEquals("12345", response.getZipCode()),
                () -> assertEquals(Category.CONSOLATION, response.getCategory())
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

        Pageable pageable = PageRequest.of(0, 5);

        RandomLetterResponse dto = RandomLetterResponse.builder()
                .letterId(1L)
                .writerId(2L)
                .title("제목블라블라")
                .zipCode("12345")
                .category(Category.ETC)
                .createdAt(LocalDateTime.now())
                .build();

        //조회 시 편지
        when(authFacade.getCurrentUserId()).thenReturn(1L);
        when(letterRepository.findRandomLettersByCategory(null, 1L, pageable)).thenReturn(List.of(dto));

        List<RandomLetterResponse> responses = randomLetterService.findRandomLetters(category);
        RandomLetterResponse response = responses.get(0);

        assertAll("랜덤 편지 응답 검증",
                () -> assertEquals("제목블라블라", response.getTitle()),
                () -> assertEquals("12345", response.getZipCode()),
                () -> assertEquals(Category.ETC, response.getCategory())
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

        CheckLastMatchResponse response = randomLetterService.checkLastMatched();
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

        CheckLastMatchResponse response = randomLetterService.checkLastMatched();
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

        CheckLastMatchResponse response = randomLetterService.checkLastMatched();
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

        TemporaryMatchingResponse response = randomLetterService.checkTemporaryMatchedTable();

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

        TemporaryMatchingResponse response = randomLetterService.checkTemporaryMatchedTable();

        assertFalse(response.isTemporary());
    }

    @Test
    @DisplayName("matchingCancel - 임시 매칭 데이터가 존재하는 경우 삭제")
    void matchingCancel_whenExists() {
        Long userId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(userId);

        Letter letter = Letter.builder()
                .content("내용입니닷")
                .category(Category.CONSOLATION)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .writerId(10L)
                .build();
        ReflectionTestUtils.setField(letter, "id", 100L);

        LetterTemporaryMatching tempMatching = LetterTemporaryMatching.builder()
                .letterId(letter.getId())
                .firstMemberId(2L)
                .secondMemberId(userId)
                .build();
        when(letterRepository.findById(tempMatching.getLetterId())).thenReturn(Optional.of(letter));
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId)).thenReturn(Optional.of(tempMatching));

        randomLetterService.matchingCancel();

        verify(letterRepository, times(1)).findById(tempMatching.getLetterId());
        verify(letterTemporaryMatchingRepository, times(1)).delete(tempMatching);
    }

    @Test
    @DisplayName("matchingCancel - 임시 매칭 데이터가 존재하지 않는 경우 에러")
    void matchingCancel_notexists() {
        Long userId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(userId);
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId)).thenReturn(Optional.empty());
        assertThrows(TemporaryMatchingNotFoundException.class, () -> randomLetterService.matchingCancel());
    }


    @Test
    @DisplayName("approveLetter - 이미 승인된 경우 AlreadyApprovedException 발생")
    void approveLetter_alreadyApproved() {
        Long userId = 1L;
        ApproveLetterRequest request = ApproveLetterRequest.builder()
                .letterId(1L)
                .writerId(2L)
                .build();
        when(authFacade.getCurrentUserId()).thenReturn(userId);
        // 이미 임시 매칭 데이터가 존재한다고 가정
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId))
                .thenReturn(Optional.of(LetterTemporaryMatching.builder().build()));
        assertThrows(AlreadyApprovedException.class, () -> randomLetterService.approveLetter(request));
    }

    @Test
    @DisplayName("approveLetter - 중복 매칭된 경우 DuplicateLetterMatchException 발생")
    void approveLetter_duplicateMatch() {
        Long userId = 1L;
        ApproveLetterRequest request = ApproveLetterRequest.builder()
                .letterId(1L)
                .writerId(2L)
                .build();
        // 현재 사용자 ID 설정
        when(authFacade.getCurrentUserId()).thenReturn(userId);
        // 기존 임시 매칭 데이터는 없다고 가정
        when(letterTemporaryMatchingRepository.findBySecondMemberId(userId))
                .thenReturn(Optional.empty());

        // memberRepository.findById(userId) 가 정상적으로 Member를 반환하도록 스텁 처리
        Member dummyMember = Member.builder()
                .email("test@example.com")
                .password("password")
                .socialUniqueId("unique_test_id")
                .role(Role.USER)
                .build();

        // 중복 매칭 상황을 재현하기 위해 save() 호출 시 DataIntegrityViolationException 발생하도록 함 -> 유니크제약조건 시 실행되는 에러
        when(letterTemporaryMatchingRepository.save(any(LetterTemporaryMatching.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate match"));

        // DuplicateLetterMatchException이 발생하는지 검증
        assertThrows(DuplicateLetterMatchException.class, () -> randomLetterService.approveLetter(request));
    }

    @Test
    @DisplayName("completeLetterMatching - 최종 랜덤 편지 매칭(작성 완료 버튼)")
    void completeLetterMatching() {
        // given
        Long currentUserId = 1L;
        CreateLetterRequest request = CreateLetterRequest.builder()
                .parentLetterId(10L)
                .build();

        // 임시 매칭 데이터 생성 (요청의 parentLetterId와 매칭)
        LetterTemporaryMatching tempMatching = LetterTemporaryMatching.builder()
                .letterId(20L)
                .firstMemberId(100L)
                .secondMemberId(200L)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(letterTemporaryMatchingRepository.findByLetterId(request.getParentLetterId()))
                .thenReturn(Optional.of(tempMatching));

        // Member 객체 생성 및 현재 사용자 ID 할당
        Member member = Member.builder()
                .email("test@example.com")
                .password("password")
                .socialUniqueId("unique_test_id")
                .role(Role.USER)
                .build();
        when(memberRepository.findById(currentUserId)).thenReturn(Optional.of(member));

        // letterFacade.createLetter(request) 호출 시 예상 LetterResponse 반환
        LetterResponse expectedResponse = LetterResponse.builder()
                .letterId(50L)
                .title("생성된 편지 제목")
                .build();

        when(letterFacade.createLetter(request)).thenReturn(expectedResponse);

        LetterMatching savedLetterMatching = LetterMatching.builder()
                .letterId(tempMatching.getLetterId())
                .firstMemberId(tempMatching.getFirstMemberId())
                .secondMemberId(tempMatching.getSecondMemberId())
                .matchedAt(tempMatching.getMatchedAt())
                .build();

        when(letterMatchingRepository.save(any(LetterMatching.class))).thenReturn(savedLetterMatching);

        LetterResponse actualResponse = randomLetterService.completeLetterMatching(request);

        // LetterMatching 저장 검증
        verify(letterMatchingRepository, times(1)).save(any(LetterMatching.class));
        // 임시 매칭 데이터 삭제 검증
        verify(letterTemporaryMatchingRepository, times(1)).delete(tempMatching);
        // Member 조회 및 업데이트 검증
        verify(memberRepository, times(1)).findById(currentUserId);
        assertEquals(expectedResponse, actualResponse);
    }

}