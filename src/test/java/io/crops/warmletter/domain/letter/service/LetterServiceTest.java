package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//LetterService 단위 테스트
@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private BadWordService  badWordService;

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
                .fontType(randomLetterRequest.getFont())
                .receiverId(randomLetterRequest.getReceiverId())
                .parentLetterId(randomLetterRequest.getParentLetterId())
                .build();

        // repository.save()가 반환할 Letter 객체 미리 준비 (다이렉트 편지, 답장)
        savedDirectLetter = Letter.builder()
                .writerId(1L)
                .letterType(LetterType.DIRECT)
                .title(directLetterRequest.getTitle())
                .content(directLetterRequest.getContent())
                .category(directLetterRequest.getCategory())
                .paperType(directLetterRequest.getPaperType())
                .fontType(directLetterRequest.getFont())
                .receiverId(directLetterRequest.getReceiverId())
                .parentLetterId(directLetterRequest.getParentLetterId())
                .build();
    }


    @Test
    @DisplayName("랜덤 편지 작성 성공 테스트")
    void writeRandomLetter_success() {
        // given: repository.save() 호출 시 미리 준비한 Letter 객체를 반환하도록 설정
        when(letterRepository.save(any(Letter.class))).thenReturn(savedRandomLetter);

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
                () -> assertNull(response.getReceiverId()),
                () -> assertNull(response.getParentLetterId()),
                () -> assertEquals(DeliveryStatus.IN_DELIVERY, response.getDeliveryStatus()),
                () -> assertNotNull(response.getDeliveryStartedAt()),
                () -> assertNotNull(response.getDeliveryCompletedAt())
        );

        verify(letterRepository).save(any(Letter.class));
    }

    @Test
    @DisplayName("주고받는 답장 편지 작성 성공 테스트")
    void writeDirectLetter_success() {
        // given: repository.save()가 답장 편지 객체를 반환하도록 설정, save호출
        when(letterRepository.save(any(Letter.class))).thenReturn(savedDirectLetter);

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
                () -> assertEquals(DeliveryStatus.IN_DELIVERY, response.getDeliveryStatus()),
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

        // repository 동작 모의: letterId에 해당하는 편지와, 부모 ID로 이전 편지 목록 조회
        when(letterRepository.findById(previousLetter4.getId())).thenReturn(Optional.of(previousLetter4));
        when(letterRepository.findLettersByParentLetterId(previousLetter4.getParentLetterId())).thenReturn(previousLetters);

        // when
        List<LetterResponse> responses = letterService.getPreviousLetters(previousLetter4.getId());

        // then
        assertAll("이전 편지 목록 검증",
                () -> assertNotNull(responses),
                () -> assertEquals(1, responses.size()),
                () -> assertEquals("A사용자 2번 편지에 대한 답장", responses.get(0).getTitle()),
                () -> assertEquals("내용 2", responses.get(0).getContent())
        );

        // repository의 각 메서드가 올바른 인자로 호출되었는지 검증
        verify(letterRepository).findById(previousLetter4.getId());
        verify(letterRepository).findLettersByParentLetterId(previousLetter4.getParentLetterId());
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
        assertTrue(letter.getIsActive(), "편지는 기본적으로 활성 상태여야 합니다.");

        // When: delete 메서드를 호출하여 소프트 딜리트 수행
        letterService.deleteLetter(1L);

        // Then: 해당 편지를 다시 조회하면 isActive가 false로 변경되어 있어야 함
        Letter deletedLetter = letterRepository.findById(1L).orElseThrow(LetterNotFoundException::new);
        assertFalse(deletedLetter.getIsActive(), "편지 삭제 후 isActive는 false여야 합니다.");
    }

    @Test
    @DisplayName("letterId로 편지 단건 조회 ")
    void getLetter_success() {
        ReflectionTestUtils.setField(savedRandomLetter, "id", 1L);

        when(letterRepository.findById(savedRandomLetter.getId())).thenReturn(Optional.of(savedRandomLetter));

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
    }
}