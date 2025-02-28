package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.EvaluateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.exception.*;
import io.crops.warmletter.domain.letter.repository.LetterMatchingRepository;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.facade.MemberFacade;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//LetterService 단위 테스트
@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private BadWordService badWordService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LetterMatchingRepository letterMatchingRepository;

    @Mock
    private AuthFacade authFacade;

    @Mock
    private MemberFacade memberFacade;

    @InjectMocks
    private LetterService letterService;


    private CreateLetterRequest randomLetterRequest;
    private CreateLetterRequest directLetterRequest;

    private Letter savedRandomLetter;
    private Letter savedDirectLetter;


    @BeforeEach
    void setUp() {
        // 랜덤편지 우편함으로 가는 첫 편지
        randomLetterRequest = CreateLetterRequest.builder()
                .receiverId(null)
                .parentLetterId(null)
                .title("랜덤 편지 제목")
                .content("랜덤 편지 내용")
                .category(Category.CONSULT)
                .paperType(PaperType.COMFORT)
                .fontType(FontType.HIMCHAN)
                .build();

        //받는사람(receiverId)과 상위 편지(parentLetterId)가 있으면 주고받는 답장 편지(DIRECT)로 작성됨
        directLetterRequest = CreateLetterRequest.builder()
                .receiverId(3L)
                .parentLetterId(5L)
                .title("답장 편지 제목")
                .content("답장 편지 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .fontType(FontType.GYEONGGI)
                .build();

        // repository.save()가 반환할 Letter 객체 미리 준비 (랜덤편지에 간 편지에 대한 첫 답장)
        savedRandomLetter = Letter.builder()
                .writerId(1L)
                .letterType(LetterType.RANDOM)
                .title(randomLetterRequest.getTitle())
                .content(randomLetterRequest.getContent())
                .category(randomLetterRequest.getCategory())
                .paperType(randomLetterRequest.getPaperType())
                .fontType(randomLetterRequest.getFontType())
                .receiverId(randomLetterRequest.getReceiverId())
                .parentLetterId(randomLetterRequest.getParentLetterId())
                .status(Status.IN_DELIVERY)
                .build();

        // repository.save()가 반환할 Letter 객체 미리 준비 (다이렉트 편지, 답장)
        savedDirectLetter = Letter.builder()
                .writerId(1L)
                .letterType(LetterType.DIRECT)
                .title(directLetterRequest.getTitle())
                .content(directLetterRequest.getContent())
                .category(directLetterRequest.getCategory())
                .paperType(directLetterRequest.getPaperType())
                .fontType(directLetterRequest.getFontType())
                .receiverId(directLetterRequest.getReceiverId())
                .parentLetterId(directLetterRequest.getParentLetterId())
                .status(Status.IN_DELIVERY)
                .build();
    }


    @Test
    @DisplayName("랜덤 편지 작성 성공 테스트")
    void writeRandomLetter_success() {
        // given: repository.save() 호출 시 미리 준비한 Letter 객체를 반환하도록 설정
        when(letterRepository.save(any(Letter.class))).thenReturn(savedRandomLetter);
        when(authFacade.getZipCode()).thenReturn("12345");

        // when: 서비스 메서드 호출
        LetterResponse response = letterService.createLetter(randomLetterRequest);

        // then: 반환된 응답 DTO 검증
        assertAll("랜덤 편지 응답 검증",
                () -> assertNotNull(response),
                () -> assertEquals("랜덤 편지 제목", response.getTitle()),
                () -> assertEquals("랜덤 편지 내용", response.getContent()),
                () -> assertEquals(Category.CONSULT, response.getCategory()),
                () -> assertEquals(PaperType.COMFORT, response.getPaperType()),
                () -> assertEquals(FontType.HIMCHAN, response.getFontType()),
                () -> assertEquals(1L, response.getWriterId()),
                () -> assertEquals("12345", authFacade.getZipCode()),
                () -> assertNull(response.getReceiverId()),
                () -> assertNull(response.getParentLetterId()),
                () -> assertEquals(Status.IN_DELIVERY, response.getStatus()),
                () -> assertNotNull(response.getDeliveryStartedAt()),
                () -> assertNotNull(response.getDeliveryCompletedAt())
        );

        verify(letterRepository).save(any(Letter.class));
    }

    @Test
    @DisplayName("편지 작성 시 비속어 검사 호출 테스트")
    void createLetter_badWordValidation() {
        // given
        when(letterRepository.save(any(Letter.class))).thenReturn(savedRandomLetter);
        when(authFacade.getZipCode()).thenReturn("12345");

        // when
        letterService.createLetter(randomLetterRequest);

        // then
        verify(badWordService).validateText(randomLetterRequest.getTitle());
        verify(badWordService).validateText(randomLetterRequest.getContent());
    }

    @Test
    @DisplayName("편지 작성 시 ParentLetterNotFoundException 에러")
    void writeRandomLetter_fail() {
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(2L)
                .parentLetterId(12345L) // 존재하지 않는 부모 편지 ID
                .title("답장 제목")
                .content("답장 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .fontType(FontType.GYEONGGI)
                .matchingId(100L)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(1L);
        when(letterRepository.findById(12345L)).thenReturn(Optional.empty());

        assertThrows(ParentLetterNotFoundException.class, () -> letterService.createLetter(request));

    }

    @Test
    @DisplayName("편지 작성 시 첫편지일때 matchingId가 잘 들어가는지 확인")
    void createLetter_directLetter_updatesParentLetter() {
        // given: 답장 편지 요청 (receiverId가 null이 아니므로 답장편지로 처리됨)
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(3L)         // 답장을 받는 사용자 ID
                .parentLetterId(10L)      // 부모 편지 ID (존재해야 함)
                .title("답장 제목")
                .content("답장 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .fontType(FontType.GYEONGGI)
                .matchingId(100L)         // 업데이트할 매칭 ID
                .build();

        // 현재 로그인한 사용자 ID는 1L라고 가정 (작성자)
        when(authFacade.getCurrentUserId()).thenReturn(1L);

        // 부모 편지 (firstLetter) 생성: 부모 편지의 parentLetterId가 null이면 첫 편지이므로 업데이트 로직이 실행됨.
        Letter firstLetter = Letter.builder()
                .writerId(2L)
                .receiverId(null)
                .parentLetterId(null)   // 부모 편지이므로 null
                .letterType(LetterType.RANDOM)
                .title("첫 편지 제목")
                .content("첫 편지 내용")
                .category(Category.CONSULT)
                .paperType(PaperType.BASIC)
                .fontType(FontType.HIMCHAN)
                .status(Status.IN_DELIVERY)
                .build();
        ReflectionTestUtils.setField(firstLetter, "id", 10L); // 부모 편지 ID 설정

        // spy 객체로 wrapping하여 update 메서드 호출 여부를 검증
        when(letterRepository.findById(request.getParentLetterId())).thenReturn(Optional.of(firstLetter));

        // 새 편지 저장 시, 답장 편지로 생성된 편지를 반환하도록 설정
        Letter newLetter = Letter.builder()
                .writerId(1L)
                .receiverId(request.getReceiverId())
                .parentLetterId(request.getParentLetterId())
                .letterType(LetterType.DIRECT)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .paperType(request.getPaperType())
                .fontType(request.getFontType())
                .status(Status.IN_DELIVERY)
                .matchingId(request.getMatchingId())
                .build();
        ReflectionTestUtils.setField(newLetter, "id", 50L);
        when(letterRepository.save(any(Letter.class))).thenReturn(newLetter);

        when(authFacade.getZipCode()).thenReturn("12345");

        // when: 서비스 메서드 호출
        LetterResponse response = letterService.createLetter(request);

        // then: spyFirstLetter의 업데이트 메서드들이 호출되었는지 검증

        // 그리고 반환된 LetterResponse가 새로 저장된 편지 정보를 반영하는지도 검증
        assertAll("답장 편지 응답 검증",
                () -> assertNotNull(response),
                () -> assertEquals(50L, response.getLetterId()),
                () -> assertEquals("답장 제목", response.getTitle()),
                () -> assertEquals("답장 내용", response.getContent()),
                () -> assertEquals(Category.ETC, response.getCategory()),
                () -> assertEquals(PaperType.PAPER, response.getPaperType()),
                () -> assertEquals(FontType.GYEONGGI, response.getFontType())
        );
    }

    @Test
    @DisplayName("답장 편지 작성 시 부모 편지가 이미 답장인 경우 부모 업데이트 로직 미실행")
    void createLetter_directLetter_noParentUpdate() {
        // given: 답장 편지 요청 (부모 편지의 parentLetterId가 이미 null이 아님)
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(3L)
                .parentLetterId(10L)
                .title("답장 제목")
                .content("답장 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .fontType(FontType.GYEONGGI)
                .matchingId(100L)
                .build();

        // 현재 로그인한 사용자 ID 설정
        when(authFacade.getCurrentUserId()).thenReturn(1L);

        // 부모 편지 생성: 이미 답장 편지인 경우이므로 parentLetterId는 null이 아님
        Letter parentLetter = Letter.builder()
                .writerId(2L)
                .receiverId(3L)
                .parentLetterId(99L) // null이 아니므로 업데이트 로직이 실행되지 않아야 함
                .letterType(LetterType.DIRECT)
                .title("부모 편지 제목")
                .content("부모 편지 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .fontType(FontType.GYEONGGI)
                .status(Status.DELIVERED)
                .build();
        ReflectionTestUtils.setField(parentLetter, "id", 10L);

        // 부모 편지 객체를 spy로 wrapping하여 update 메서드 호출 여부를 검증
        Letter parentLetterSpy = spy(parentLetter);
        when(letterRepository.findById(request.getParentLetterId())).thenReturn(Optional.of(parentLetterSpy));

        // 새 편지 저장을 위한 설정
        Letter newLetter = Letter.builder()
                .writerId(1L)
                .receiverId(request.getReceiverId())
                .parentLetterId(request.getParentLetterId())
                .letterType(LetterType.DIRECT)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .paperType(request.getPaperType())
                .fontType(request.getFontType())
                .status(Status.IN_DELIVERY)
                .matchingId(request.getMatchingId())
                .build();
        ReflectionTestUtils.setField(newLetter, "id", 50L);

        when(letterRepository.save(any(Letter.class))).thenReturn(newLetter);
        when(authFacade.getZipCode()).thenReturn("12345");

        // when: 서비스 메서드 호출
        LetterResponse response = letterService.createLetter(request);

        // then: 반환된 응답 검증
        assertAll("답장 편지 응답 검증",
                () -> assertNotNull(response),
                () -> assertEquals(50L, response.getLetterId()),
                () -> assertEquals("답장 제목", response.getTitle()),
                () -> assertEquals("답장 내용", response.getContent())
        );

        // then: 부모 편지에 대한 업데이트 메서드가 호출되지 않았음을 검증
        verify(parentLetterSpy, never()).updateIsRead(anyBoolean());
    }

    @Test
    @DisplayName("주고받는 답장 편지 작성 성공 테스트")
    void writeDirectLetter_success() {
        // given: repository.save()가 답장 편지 객체를 반환하도록 설정, save호출
        when(letterRepository.save(any(Letter.class))).thenReturn(savedDirectLetter);
        //현재 로그인한 사용자 ID 및 ZipCode 설정
        when(authFacade.getCurrentUserId()).thenReturn(1L);
        when(authFacade.getZipCode()).thenReturn("12345");

        // 부모 편지에 해당하는 객체 생성 (예: parentLetter)
        Letter parentLetter = Letter.builder()
                .writerId(2L)
                .receiverId(1L)
                .parentLetterId(null)
                .letterType(LetterType.RANDOM)
                .category(Category.CONSULT)
                .title("부모 편지 제목")
                .content("부모 편지 내용")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .status(Status.DELIVERED) // 또는 적절한 상태
                .build();
        ReflectionTestUtils.setField(parentLetter, "id", 5L);
        when(letterRepository.findById(directLetterRequest.getParentLetterId())).thenReturn(Optional.of(parentLetter));

        // when: 서비스 메서드 호출
        LetterResponse response = letterService.createLetter(directLetterRequest);

        // then: 반환된 응답 DTO 검증
        assertAll("답장 편지 응답 검증",
                () -> assertNotNull(response),
                () -> assertEquals("답장 편지 제목", response.getTitle()),
                () -> assertEquals("답장 편지 내용", response.getContent()),
                () -> assertEquals(Category.ETC, response.getCategory()),
                () -> assertEquals(PaperType.PAPER, response.getPaperType()),
                () -> assertEquals(FontType.GYEONGGI, response.getFontType()),
                () -> assertEquals(1L, response.getWriterId()),
                () -> assertEquals(3L, response.getReceiverId()),
                () -> assertEquals(5L, response.getParentLetterId()),
                () -> assertEquals("12345", authFacade.getZipCode()),
                () -> assertEquals(Status.IN_DELIVERY, response.getStatus()),
                () -> assertNotNull(response.getDeliveryStartedAt()),
                () -> assertNotNull(response.getDeliveryCompletedAt())
        );
        //verify 메서드로 letterRepository.save() 메서드가 정확히 1번 호출되었는지 확인
        verify(letterRepository).save(any(Letter.class));
    }


    @Test
    @DisplayName("이전 편지 목록 조회 성공 테스트")
    void getPreviousLetters_success() {
        // 현재 사용자 ID 설정
        Long myId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(myId);

        Long replyLetterId = 5L;
        Long parentLetterId = 10L;
        Long matchingId = 100L;
        Letter replyLetter = Letter.builder()
                .writerId(myId) // 답장 쓴 사람이 현재 사용자라고 가정
                .parentLetterId(parentLetterId)
                .matchingId(matchingId)
                .title("답장 제목")
                .content("답장 내용")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .status(Status.DELIVERED)
                .build();
        ReflectionTestUtils.setField(replyLetter, "id", replyLetterId);

        // 부모 편지: 답장 편지의 부모 편지 (부모 편지의 정보는 이전 편지 목록 조회에 사용)
        Letter parentLetter = Letter.builder()
                .writerId(2L)
                .title("부모 편지 제목")
                .content("부모 편지 내용")
                .fontType(FontType.KYOBO)
                .paperType(PaperType.BASIC)
                .status(Status.DELIVERED)
                .build();
        ReflectionTestUtils.setField(parentLetter, "id", parentLetterId);

        // 매칭 정보
        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(myId)
                .secondMemberId(2L)
                .build();
        ReflectionTestUtils.setField(matching, "id", matchingId);

        // 이전 편지 목록: 부모 편지를 기준으로 조회된 편지들
        Letter previousLetter = Letter.builder()
                .writerId(2L)
                .receiverId(myId)
                .parentLetterId(parentLetterId)
                .letterType(LetterType.DIRECT)
                .title("이전 편지 제목")
                .content("이전 편지 내용")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .status(Status.DELIVERED)
                .build();
        ReflectionTestUtils.setField(previousLetter, "id", 11L);
        List<Letter> previousLetters = List.of(previousLetter);

        when(letterRepository.findById(replyLetterId)).thenReturn(Optional.of(replyLetter));
        when(letterRepository.findLettersByParentLetterId(parentLetterId)).thenReturn(previousLetters);
        when(letterMatchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(Member.builder().zipCode("12345").build()));

        // when
        List<LetterResponse> responses = letterService.getPreviousLetters(replyLetterId);

        // then
        assertAll("이전 편지 목록 검증",
                () -> assertNotNull(responses),
                () -> assertEquals(1, responses.size()),
                () -> assertEquals("이전 편지 제목", responses.get(0).getTitle()),
                () -> assertEquals("이전 편지 내용", responses.get(0).getContent()),
                () -> assertEquals("12345", responses.get(0).getZipCode())
        );

        verify(letterRepository).findById(replyLetterId);
        verify(letterRepository).findLettersByParentLetterId(parentLetterId);
        verify(letterMatchingRepository).findById(matchingId);
        verify(memberRepository).findById(2L);
    }

    @Test
    @DisplayName("getPreviousLetters - 매칭 정보가 없으면 MatchingNotFoundException 발생")
    void getPreviousLetters_matchingNotFound() {
        // given
        Long myId = 1L;
        Long letterId = 5L; // 테스트용 편지 ID
        Long parentLetterId = 10L;
        Long matchingId = 100L; // 매칭 정보가 없는 경우

        when(authFacade.getCurrentUserId()).thenReturn(myId);

        Letter letter = Letter.builder()
                .writerId(2L)
                .parentLetterId(parentLetterId)
                .matchingId(matchingId)
                .build();
        ReflectionTestUtils.setField(letter, "id", letterId);
        when(letterRepository.findById(letterId)).thenReturn(Optional.of(letter));

        when(letterMatchingRepository.findById(matchingId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(MatchingNotFoundException.class, () -> letterService.getPreviousLetters(letterId));

        // verify 호출 여부 확인
        verify(letterRepository).findById(letterId);
        verify(letterMatchingRepository).findById(matchingId);
    }

    @Test
    @DisplayName("getPreviousLetters - 현재 사용자가 매칭에 속하지 않은 경우 MatchingNotBelongException 발생")
    void getPreviousLetters_whenUserNotBelong() {
        // given
        Long currentUserId = 1L; // 현재 로그인한 사용자 ID
        Long replyLetterId = 5L; // 답장 편지의 ID
        Long parentLetterId = 10L; // 답장 편지의 부모 편지 ID
        Long matchingId = 100L;    // 답장 편지에 설정된 매칭 ID

        // 답장 편지 생성
        Letter replyLetter = Letter.builder()
                .writerId(2L) // 작성자는 2번 (현재 사용자는 1번)
                .parentLetterId(parentLetterId)
                .matchingId(matchingId)
                .title("답장 편지 제목")
                .content("답장 편지 내용")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .status(Status.DELIVERED)
                .build();
        ReflectionTestUtils.setField(replyLetter, "id", replyLetterId);

        // 매칭 정보 생성 (매칭된 두 사용자 모두 현재 사용자가 아닌 값으로 설정)
        LetterMatching letterMatching = LetterMatching.builder()
                .firstMemberId(2L)
                .secondMemberId(3L)
                .build();
        ReflectionTestUtils.setField(letterMatching, "id", matchingId);

        when(authFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(letterRepository.findById(replyLetterId)).thenReturn(Optional.of(replyLetter)); // 답장 편지 조회 시
        when(letterMatchingRepository.findById(matchingId)).thenReturn(Optional.of(letterMatching));

        // when & then
        assertThrows(MatchingNotBelongException.class, () -> {
            letterService.getPreviousLetters(replyLetterId);
        });

        // verify: 해당 메서드들이 호출되었는지 확인
        verify(letterRepository).findById(replyLetterId);
        verify(letterMatchingRepository).findById(matchingId);
    }




    @Test
    @DisplayName("이전 편지 목록 조회 실패 테스트 - 존재하지 않는 편지")
    void getPreviousLetters_fail() {
        // given: 존재하지 않는 편지 ID 설정
        Long nonExistentLetterId = 1000L;
        when(letterRepository.findById(nonExistentLetterId)).thenReturn(Optional.empty());

        // when, then: 해당 편지 ID로 조회 시 LetterNotFoundException이 발생하는지 검증
        assertThrows(LetterNotFoundException.class, () -> {letterService.getPreviousLetters(nonExistentLetterId);});

        // repository의 findById가 해당 ID로 호출되었는지 검증 호출 되면 통과
        verify(letterRepository).findById(nonExistentLetterId);
    }

    @Test
    @DisplayName("소프트 딜리트 테스트 - 편지 삭제 후 isActive가 false로 변경되어야 한다.")
    void delete_softDelete_success() {
        // Given: 새로운 편지를 생성하여 저장 (단, 목 객체이므로 직접 ID를 주입)
        Letter letter = Letter.builder()
                .writerId(1L)
                .receiverId(null)
                .parentLetterId(null)
                .letterType(LetterType.RANDOM)
                .category(Category.CONSULT)
                .title("테스트 편지 제목")
                .content("테스트 편지 내용")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .build();

        // 테스트에서는 ID를 직접 설정합니다.
        ReflectionTestUtils.setField(letter, "id", 1L);

        // 또한, delete() 메서드에서 letterRepository.findById() 호출 시 해당 엔티티를 반환하도록 스텁합니다.
        when(letterRepository.findById(1L)).thenReturn(Optional.of(letter));

        // 검증: 기본적으로 편지는 활성 상태여야 함
        assertTrue(letter.isActive(), "편지는 기본적으로 활성 상태여야 합니다.");

        // When: delete 메서드를 호출하여 소프트 딜리트 수행
        letterService.deleteLetter(1L);

        // Then: 해당 편지를 다시 조회하면 isActive가 false로 변경되어 있어야 함
        Letter deletedLetter = letterRepository.findById(1L).orElseThrow(LetterNotFoundException::new);
        assertFalse(deletedLetter.isActive(), "편지 삭제 후 isActive는 false여야 합니다.");
    }

    @Test
    @DisplayName("letterId로 편지 단건 조회 성공")
    void getLetter_success() {
        Member member = Member.builder()
                .zipCode("12345")
                .build();
        ReflectionTestUtils.setField(member, "id", savedRandomLetter.getWriterId());
        ReflectionTestUtils.setField(savedRandomLetter, "id", 1L);

        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(member.getId())
                .secondMemberId(99L).build();

        when(authFacade.getCurrentUserId()).thenReturn(member.getId());
        when(letterRepository.findById(savedRandomLetter.getId())).thenReturn(Optional.of(savedRandomLetter));
        when(memberRepository.findById(savedRandomLetter.getWriterId())).thenReturn(Optional.of(member));
        when(letterMatchingRepository.findById(savedRandomLetter.getMatchingId())).thenReturn(Optional.ofNullable(matching));

        LetterResponse response = letterService.getLetterById(savedRandomLetter.getId());

        // then
        assertAll("답장 조회 응답 검증",
                () -> assertNotNull(response),
                () -> assertNotNull(response.getLetterId()),
                () -> assertEquals("랜덤 편지 제목", response.getTitle()),
                () -> assertEquals("랜덤 편지 내용", response.getContent()),
                () -> assertEquals(PaperType.COMFORT, response.getPaperType()),
                () -> assertEquals(FontType.HIMCHAN, response.getFontType())
        );
        verify(letterRepository).findById(savedRandomLetter.getId());
        verify(memberRepository).findById(savedRandomLetter.getWriterId());
    }

    @Test
    @DisplayName("letterId로 편지 단건 조회 실패 ")
    void getLetter_fail() {
        ReflectionTestUtils.setField(savedRandomLetter, "id", 1L);
        ReflectionTestUtils.setField(savedRandomLetter, "matchingId", 100L);

        when(authFacade.getCurrentUserId()).thenReturn(10L);

        when(letterRepository.findById(1L)).thenReturn(Optional.of(savedRandomLetter));

        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(22L)
                .secondMemberId(99L)
                .build();
        ReflectionTestUtils.setField(matching, "id", 100L);
        when(letterMatchingRepository.findById(100L)).thenReturn(Optional.of(matching));

        assertThrows(MatchingNotBelongException.class, () -> letterService.getLetterById(1L));
    }

    @Test
    @DisplayName("getLetterById - 편지 조회 시 읽음 상태로 업데이트")
    void getLetterById_updatesReadStatus() {
        // given
        Long letterId = 1L;
        Long myId = 1L;

        Letter letter = Letter.builder()
                .writerId(2L)
                .receiverId(myId)
                .matchingId(100L)
                .letterType(LetterType.DIRECT)
                .title("테스트 제목")
                .content("테스트 내용")
                .build();
        ReflectionTestUtils.setField(letter, "id", letterId);
        ReflectionTestUtils.setField(letter, "isRead", false);

        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(myId)
                .secondMemberId(2L)
                .build();
        ReflectionTestUtils.setField(matching, "id", 100L);

        Member writer = Member.builder()
                .zipCode("12345")
                .build();
        ReflectionTestUtils.setField(writer, "id", 2L);

        when(authFacade.getCurrentUserId()).thenReturn(myId);
        when(letterRepository.findById(letterId)).thenReturn(Optional.of(letter));
        when(letterMatchingRepository.findById(100L)).thenReturn(Optional.of(matching));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(writer));

        // when
        letterService.getLetterById(letterId);

        // then
        assertTrue(letter.isRead());
        verify(letterRepository).save(letter);
    }

    @DisplayName("편지 평가 실패 - 편지에 대해 평가할 수 있는 권한 없음")
    @Test
    void evaluateLetter_WithNotBelongLetter_ShouldThrowException() throws Exception {
        //given
        Long memberId = 1L;
        Long invalidLetterId = 1L;
        EvaluateLetterRequest request = new EvaluateLetterRequest();
        LetterEvaluation evaluation = LetterEvaluation.GOOD;

        Field evaluationField = EvaluateLetterRequest.class.getDeclaredField("evaluation");
        evaluationField.setAccessible(true);
        evaluationField.set(request, evaluation);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(letterRepository.findByIdAndReceiverId(invalidLetterId, memberId)).thenReturn(Optional.empty());

        //when & then
        assertThrows(LetterNotBelongException.class,
                () -> letterService.evaluateLetter(invalidLetterId, request));

        verify(authFacade).getCurrentUserId();
        verify(letterRepository).findByIdAndReceiverId(memberId, invalidLetterId);
    }

    @DisplayName("편지 평가 성공")
    @Test
    void evaluateLetter_Success() throws Exception {
        //given
        Long writerId = 2L;
        Long receiverId = 1L;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1AA2C")
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, receiverId);

        Long letterId = 1L;
        Letter letter = Letter.builder()
                .writerId(writerId)
                .receiverId(receiverId)
                .letterType(LetterType.DIRECT)
                .category(Category.CONSULT)
                .title("A사용자 2번 편지에 대한 답장")
                .content("내용 2")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .build();
        // Reflection으로 id 설정
        Field letterIdField = Letter.class.getDeclaredField("id");
        letterIdField.setAccessible(true);
        letterIdField.set(letter, letterId);


        EvaluateLetterRequest request = new EvaluateLetterRequest();
        LetterEvaluation evaluation = LetterEvaluation.GOOD;

        Field evaluationField = EvaluateLetterRequest.class.getDeclaredField("evaluation");
        evaluationField.setAccessible(true);
        evaluationField.set(request, evaluation);

        when(authFacade.getCurrentUserId()).thenReturn(receiverId);
        when(letterRepository.findByIdAndReceiverId(letterId, receiverId)).thenReturn(Optional.of(letter));

        //when
        letterService.evaluateLetter(letterId, request);

        //then
        verify(memberFacade).applyEvaluationTemperature(writerId, evaluation);
    }
}