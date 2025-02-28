package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.EvaluateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.TemporarySaveLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.exception.LetterNotBelongException;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.exception.ParentLetterNotFoundException;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
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
                .font(FontType.HIMCHAN)
                .build();

        //받는사람(receiverId)과 상위 편지(parentLetterId)가 있으면 주고받는 답장 편지(DIRECT)로 작성됨
        directLetterRequest = CreateLetterRequest.builder()
                .receiverId(3L)
                .parentLetterId(5L)
                .title("답장 편지 제목")
                .content("답장 편지 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .font(FontType.GYEONGGI)
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
    @DisplayName("편지 작성 시 ParentLetterNotFoundException 에러")
    void writeRandomLetter_fail() {
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(2L)
                .parentLetterId(12345L) // 존재하지 않는 부모 편지 ID
                .title("답장 제목")
                .content("답장 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .font(FontType.GYEONGGI)
                .matchingId(100L)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(1L);
        when(letterRepository.findById(12345L)).thenReturn(Optional.empty());

        assertThrows(ParentLetterNotFoundException.class, () -> letterService.createLetter(request));

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

        // 최초의 편지 A사용자
        Letter currentLetter = Letter.builder()
                .writerId(1L)
                .receiverId(null)
                .parentLetterId(null)
                .letterType(LetterType.RANDOM)
                .category(Category.ETC)
                .title("현재 편지 제목")
                .content("현재 편지 내용")
                .fontType(FontType.GYEONGGI)
                .paperType(PaperType.PAPER)
                .build();
        ReflectionTestUtils.setField(currentLetter, "id", 1L);

        //B 사용자
        Letter previousLetter1 = Letter.builder()
                .writerId(2L)
                .receiverId(1L)
                .parentLetterId(currentLetter.getId())
                .letterType(LetterType.DIRECT)
                .category(Category.CONSULT)
                .title("B 사용자 1번 편지에 대한 답장")
                .content("내용 1")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .build();
        ReflectionTestUtils.setField(previousLetter1, "id", 2L);

        //B 사용자
        Letter previousLetter2 = Letter.builder()
                .writerId(2L)
                .receiverId(1L)
                .parentLetterId(currentLetter.getId())
                .letterType(LetterType.DIRECT)
                .category(Category.CONSULT)
                .title("B사용자 1번 편지에 대한 2번째 답장")
                .content("내용 2")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .build();
        ReflectionTestUtils.setField(previousLetter2, "id", 3L);

        //A 사용자
        Letter previousLetter3 = Letter.builder()
                .writerId(1L)
                .receiverId(2L)
                .parentLetterId(previousLetter1.getId())
                .letterType(LetterType.DIRECT)
                .category(Category.CONSULT)
                .title("A사용자 1번 편지에 대한 답장")
                .content("내용 2")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .build();
        ReflectionTestUtils.setField(previousLetter3, "id", 4L);

        //A 사용자
        Letter previousLetter4 = Letter.builder()
                .writerId(1L)
                .receiverId(2L)
                .parentLetterId(previousLetter2.getId())
                .letterType(LetterType.DIRECT)
                .category(Category.CONSULT)
                .title("A사용자 2번 편지에 대한 답장")
                .content("내용 2")
                .fontType(FontType.HIMCHAN)
                .paperType(PaperType.COMFORT)
                .build();
        ReflectionTestUtils.setField(previousLetter4, "id", 5L);

        List<Letter> previousLetters = List.of(previousLetter4);

        //zipCode 추가로 맴버 추가
        Member member = Member.builder()
                .zipCode("12345")
                .build();
        ReflectionTestUtils.setField(member, "id", 1L); //아이디

        // repository: letterId에 해당하는 편지와, 부모 ID로 이전 편지 목록 조회
        when(letterRepository.findById(previousLetter4.getId())).thenReturn(Optional.of(previousLetter4));
        when(letterRepository.findLettersByParentLetterId(previousLetter4.getParentLetterId())).thenReturn(previousLetters);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        List<LetterResponse> responses = letterService.getPreviousLetters(previousLetter4.getId());

        // then
        assertAll("이전 편지 목록 검증",
                () -> assertNotNull(responses),
                () -> assertEquals(1, responses.size()),
                () -> assertEquals("A사용자 2번 편지에 대한 답장", responses.get(0).getTitle()),
                () -> assertEquals("내용 2", responses.get(0).getContent()),
                () -> assertEquals("12345", member.getZipCode())
        );

        // repository의 각 메서드가 올바른 인자로 호출되었는지 검증
        verify(letterRepository).findById(previousLetter4.getId());
        verify(letterRepository).findLettersByParentLetterId(previousLetter4.getParentLetterId());
        verify(memberRepository).findById(1L);
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
    @DisplayName("letterId로 편지 단건 조회 ")
    void getLetter_success() {
        ReflectionTestUtils.setField(savedRandomLetter, "id", 1L);

        when(letterRepository.findById(savedRandomLetter.getId())).thenReturn(Optional.of(savedRandomLetter));

        Member member = Member.builder()
                .zipCode("12345")
                .build();
        // savedRandomLetter의 writerId와 동일한 값으로 설정 (예를 들어 1L)
        ReflectionTestUtils.setField(member, "id", savedRandomLetter.getWriterId());
        when(memberRepository.findById(savedRandomLetter.getWriterId())).thenReturn(Optional.of(member));

        LetterResponse response = letterService.getLetterById(savedRandomLetter.getId());

        // then: 반환된 응답 DTO 검증
        assertAll("답장 조회 응답 검증",
                () -> assertNotNull(response),
                () -> assertNotNull(response.getLetterId()),
                () -> assertEquals("랜덤 편지 제목", response.getTitle()),
                () -> assertEquals("랜덤 편지 내용", response.getContent()),
                () -> assertEquals(PaperType.COMFORT, response.getPaperType()),
                () -> assertEquals(FontType.HIMCHAN, response.getFontType())
        );

        //verify 메서드로 letterRepository.save() 메서드가 정확히 1번 호출되었는지 확인
        verify(letterRepository).findById(savedRandomLetter.getId());
        verify(memberRepository).findById(savedRandomLetter.getWriterId());

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
    @Test
    @DisplayName("기존 편지 임시 저장 성공 테스트")
    void temporarySaveExistingLetter_success() throws Exception {
        // given
        Long letterId = 1L;
        Long writerId = 1L;

        // Letter를 mock으로 생성
        Letter existingLetter = mock(Letter.class);
        // mock 객체에 필요한 getter 메서드 동작 정의
        when(existingLetter.getId()).thenReturn(letterId);
        when(existingLetter.getTitle()).thenReturn("임시 저장 제목");
        when(existingLetter.getContent()).thenReturn("임시 저장 내용");
        when(existingLetter.getCategory()).thenReturn(Category.ETC);
        when(existingLetter.getFontType()).thenReturn(FontType.HIMCHAN);
        when(existingLetter.getPaperType()).thenReturn(PaperType.COMFORT);
        when(existingLetter.getStatus()).thenReturn(Status.SAVED);

        // 임시 저장 요청 객체 생성
        TemporarySaveLetterRequest request = new TemporarySaveLetterRequest();
        Field titleField = TemporarySaveLetterRequest.class.getDeclaredField("title");
        Field contentField = TemporarySaveLetterRequest.class.getDeclaredField("content");
        Field categoryField = TemporarySaveLetterRequest.class.getDeclaredField("category");
        Field fontField = TemporarySaveLetterRequest.class.getDeclaredField("fontType");
        Field paperTypeField = TemporarySaveLetterRequest.class.getDeclaredField("paperType");
        Field receiverIdField = TemporarySaveLetterRequest.class.getDeclaredField("receiverId");
        Field parentLetterIdField = TemporarySaveLetterRequest.class.getDeclaredField("parentLetterId");

        titleField.setAccessible(true);
        contentField.setAccessible(true);
        categoryField.setAccessible(true);
        fontField.setAccessible(true);
        paperTypeField.setAccessible(true);
        receiverIdField.setAccessible(true);
        parentLetterIdField.setAccessible(true);

        titleField.set(request, "임시 저장 제목");
        contentField.set(request, "임시 저장 내용");
        categoryField.set(request, Category.ETC);
        fontField.set(request, FontType.HIMCHAN);
        paperTypeField.set(request, PaperType.COMFORT);
        receiverIdField.set(request, null);
        parentLetterIdField.set(request, null);

        // 서비스 메서드 모킹
        when(letterRepository.findByIdAndWriterId(letterId, writerId)).thenReturn(Optional.of(existingLetter));
        when(authFacade.getCurrentUserId()).thenReturn(writerId);
        when(authFacade.getZipCode()).thenReturn("12345");

        // void 메서드 모킹 (doNothing 사용)
        doNothing().when(existingLetter).updateTemporarySave(
                request.getReceiverId(),
                request.getParentLetterId(),
                request.getCategory(),
                request.getTitle(),
                request.getContent()
        );

        // when
        LetterResponse response = letterService.temporarySaveLetter(letterId, request);

        // then
        assertAll("기존 편지 임시 저장 응답 검증",
                () -> assertNotNull(response),
                () -> assertEquals(letterId, response.getLetterId()),
                () -> assertEquals("임시 저장 제목", response.getTitle()),
                () -> assertEquals("임시 저장 내용", response.getContent()),
                () -> assertEquals(Category.ETC, response.getCategory()),
                () -> assertEquals(FontType.HIMCHAN, response.getFontType()),
                () -> assertEquals(PaperType.COMFORT, response.getPaperType()),
                () -> assertEquals(Status.SAVED, response.getStatus()),
                () -> assertEquals("12345", response.getZipCode())
        );

        verify(letterRepository).findByIdAndWriterId(letterId, writerId);
        verify(existingLetter).updateTemporarySave(
                request.getReceiverId(),
                request.getParentLetterId(),
                request.getCategory(),
                request.getTitle(),
                request.getContent()
        );
    }


    @Test
    @DisplayName("새 편지 임시 저장 성공 테스트")
    void temporarySaveNewLetter_success() throws Exception {
        // given
        Long writerId = 1L;
        Long newLetterId = 1L;

        TemporarySaveLetterRequest request = new TemporarySaveLetterRequest();
        Field titleField = TemporarySaveLetterRequest.class.getDeclaredField("title");
        Field contentField = TemporarySaveLetterRequest.class.getDeclaredField("content");
        Field categoryField = TemporarySaveLetterRequest.class.getDeclaredField("category");
        Field fontField = TemporarySaveLetterRequest.class.getDeclaredField("fontType");
        Field paperTypeField = TemporarySaveLetterRequest.class.getDeclaredField("paperType");
        Field parentLetterIdField = TemporarySaveLetterRequest.class.getDeclaredField("parentLetterId");
        Field receiverIdField = TemporarySaveLetterRequest.class.getDeclaredField("receiverId");

        titleField.setAccessible(true);
        contentField.setAccessible(true);
        categoryField.setAccessible(true);
        fontField.setAccessible(true);
        paperTypeField.setAccessible(true);
        parentLetterIdField.setAccessible(true);
        receiverIdField.setAccessible(true);

        titleField.set(request, "새 임시 저장 제목");
        contentField.set(request, "새 임시 저장 내용");
        categoryField.set(request, Category.ETC);
        fontField.set(request, FontType.GYEONGGI);
        paperTypeField.set(request, PaperType.PAPER);
        parentLetterIdField.set(request, null);
        receiverIdField.set(request, null);

        // 서비스 메서드 모킹
        when(authFacade.getCurrentUserId()).thenReturn(writerId);
        when(authFacade.getZipCode()).thenReturn("12345");

        // 중요: letterRepository.save() 메서드가 호출될 때 Letter 객체를 캡처하고 ID를 설정한 후 반환
        doAnswer(invocation -> {
            Letter letterToSave = invocation.getArgument(0);
            // 저장 시점에 ID 설정
            ReflectionTestUtils.setField(letterToSave, "id", newLetterId);
            return letterToSave;
        }).when(letterRepository).save(any(Letter.class));

        // when
        LetterResponse response = letterService.temporarySaveLetter(null, request);

        // then
        assertAll("새 편지 임시 저장 응답 검증",
                () -> assertNotNull(response),
                () -> assertEquals(newLetterId, response.getLetterId()),
                () -> assertEquals("새 임시 저장 제목", response.getTitle()),
                () -> assertEquals("새 임시 저장 내용", response.getContent()),
                () -> assertEquals(Category.ETC, response.getCategory()),
                () -> assertEquals(FontType.GYEONGGI, response.getFontType()),
                () -> assertEquals(PaperType.PAPER, response.getPaperType()),
                () -> assertEquals(Status.SAVED, response.getStatus()),
                () -> assertNull(response.getParentLetterId()),
                () -> assertEquals("12345", response.getZipCode())
        );

        verify(authFacade).getCurrentUserId();
        verify(letterRepository).save(any(Letter.class));
    }
}