package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class LetterServiceTest {

    @Autowired
    private LetterService letterService;

    @Autowired
    private LetterRepository letterRepository;

    @BeforeEach
    void clean() {
        letterRepository.deleteAll();
    }


    @Test
    @DisplayName("랜덤 편지로 가는(첫 편지) 작성 성공 테스트")
    void writeRandomLetter_success() {
        // given: 받는사람과 상위편지가 없으면 첫 편지(RANDOM)로 작성됨
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(null)
                .parentLetterId(null)
                .title("랜덤 편지 제목")
                .content("랜덤 편지 내용")
                .category(Category.CONSULT)
                .paperType(PaperType.COMFORT)
                .font(FontType.HIMCHAN)
                .build();

        // when
        LetterResponse response = letterService.write(request);

        // then
        // 응답 DTO 검증
        assertNotNull(response);
        assertEquals("랜덤 편지 제목", response.getTitle());
        assertEquals("랜덤 편지 내용", response.getContent());
        assertEquals(Category.CONSULT, response.getCategory());
        assertEquals(PaperType.COMFORT, response.getPaperType());
        assertEquals(FontType.HIMCHAN, response.getFont());

        // writerId는 하드코딩 되어 1L로 설정되어 있음
        assertEquals(1L, response.getWriterId());

        // 첫 편지인 경우 receiverId, parentLetterId는 null이어야 함
        assertNull(response.getReceiverId());
        assertNull(response.getParentLetterId());

        // 배송 상태 및 시간 관련 필드는 기본값으로 설정됨
        assertEquals(DeliveryStatus.IN_DELIVERY, response.getDeliveryStatus());
        assertNotNull(response.getDeliveryStartedAt());
        assertNotNull(response.getDeliveryCompletedAt());

        // 저장된 엔티티 검증 (추가적으로 DB에 정상 저장되었는지 확인)
        Optional<Letter> savedLetterOpt = letterRepository.findById(response.getLetterId());
        assertTrue(savedLetterOpt.isPresent(),"지정된 편지가 존재하지 않습니다!");

        Letter savedLetter = savedLetterOpt.get();
        assertEquals("랜덤 편지 제목", savedLetter.getTitle());
        assertEquals("랜덤 편지 내용", savedLetter.getContent());

        // 랜덤 편지인 경우 letterType은 RANDOM이어야 함
        assertEquals(LetterType.RANDOM, savedLetter.getLetterType());
    }

    @Test
    @DisplayName("주고받는 답장 편지 작성 성공 테스트")
    void writeDirectLetter_success() {
        // given: 받는사람이 존재하면 주고받는 답장 편지(DIRECT)로 작성됨
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(3L)
                .parentLetterId(5L)
                .title("답장 편지 제목")
                .content("답장 편지 내용")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .font(FontType.GYEONGGI)
                .build();

        // when
        LetterResponse response = letterService.write(request);

        // then
        // 응답 DTO 검증
        assertNotNull(response);
        assertEquals("답장 편지 제목", response.getTitle());
        assertEquals("답장 편지 내용", response.getContent());
        assertEquals(Category.ETC, response.getCategory());
        assertEquals(PaperType.PAPER, response.getPaperType());
        assertEquals(FontType.GYEONGGI, response.getFont());
        assertEquals(1L, response.getWriterId());

        // 답장 편지인 경우 receiverId, parentLetterId가 요청 값과 일치해야 함
        assertEquals(3L, response.getReceiverId());
        assertEquals(5L, response.getParentLetterId());
        assertEquals(DeliveryStatus.IN_DELIVERY, response.getDeliveryStatus());
        assertNotNull(response.getDeliveryStartedAt());
        assertNotNull(response.getDeliveryCompletedAt());

        // 저장된 엔티티 검증
        Optional<Letter> savedLetterOpt = letterRepository.findById(response.getLetterId());
        assertTrue(savedLetterOpt.isPresent(), "지정된 편지가 존재하지 않습니다!");

        Letter savedLetter = savedLetterOpt.get();
        assertEquals("답장 편지 제목", savedLetter.getTitle());
        assertEquals("답장 편지 내용", savedLetter.getContent());

        // 답장 편지인 경우 letterType은 DIRECT이어야 함
        assertEquals(LetterType.DIRECT, savedLetter.getLetterType());
    }
}