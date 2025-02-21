package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.service.LetterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
@SpringBootTest
class LettersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LetterRepository lettersRepository;

    @Autowired
    private LetterService letterService;

    @BeforeEach
    void clean() {
        lettersRepository.deleteAll();
    }

    @Test
    @DisplayName("/api/letters 요청 시 첫 편지 생성 후 값 출력 확인")
    void create_first_letter_success() throws Exception {
        //given
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(null)
                .parentLetterId(null)
                .title("제목입니다")
                .content("편지 내용입니다")
                .category(Category.CONSOLATION)
                .paperType(PaperType.BASIC)
                .font(FontType.KYOBO)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/letters")
                .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterId").exists())
                .andExpect(jsonPath("$.data.writerId").exists()) //todo 회원 넣어서 확인
                .andExpect(jsonPath("$.data.receiverId").doesNotExist())
                .andExpect(jsonPath("$.data.parentLetterId").doesNotExist())
                .andExpect(jsonPath("$.data.title").value("제목입니다"))
                .andExpect(jsonPath("$.data.content").value("편지 내용입니다"))
                .andExpect(jsonPath("$.data.category").value("CONSOLATION"))
                .andExpect(jsonPath("$.data.paperType").value("BASIC"))
                .andExpect(jsonPath("$.data.fontType").value("KYOBO"))
                .andExpect(jsonPath("$.data.deliveryStatus").value("IN_DELIVERY"))
                .andExpect(jsonPath("$.message").value("편지가 성공적으로 생성되었습니다."))
                .andDo(print());

        //then
        assertEquals(1L, lettersRepository.count());

    }

    @Test
    @DisplayName("/api/letters 요청 시 랜덤 편지 답장, 주고 받는 편지 값 출력 확인")
    void create_exchanged_letter_success() throws Exception {
        //given
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(1L) //편지 받는 사람 id
                .parentLetterId(2L) //상위 편지 id
                .title("제목입니다")
                .content("편지 내용입니다")
                .category(Category.CONSULT)
                .paperType(PaperType.COMFORT)
                .font(FontType.HIMCHAN)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/letters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterId").exists())
                .andExpect(jsonPath("$.data.writerId").exists())  //todo 회원 넣어서 확인
                .andExpect(jsonPath("$.data.receiverId").value(1L))
                .andExpect(jsonPath("$.data.parentLetterId").value(2L))
                .andExpect(jsonPath("$.data.title").value("제목입니다"))
                .andExpect(jsonPath("$.data.content").value("편지 내용입니다"))
                .andExpect(jsonPath("$.data.category").value("CONSULT"))
                .andExpect(jsonPath("$.data.paperType").value("COMFORT"))
                .andExpect(jsonPath("$.data.fontType").value("HIMCHAN"))
                .andExpect(jsonPath("$.data.deliveryStatus").value("IN_DELIVERY"))
                .andExpect(jsonPath("$.message").value("편지가 성공적으로 생성되었습니다."))
                .andDo(print());

        //then
        assertEquals(1L, lettersRepository.count());
    }

    @Test
    @DisplayName("/api/letters 요청 시 title 값은 필수다.")
    void required_title_value() throws Exception {
        //given
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(null)
                .parentLetterId(null)
                .title("")
                .content("편지 내용입니다")
                .category(Category.CONSULT)
                .paperType(PaperType.COMFORT)
                .font(FontType.HIMCHAN)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/letters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COM-001"))
                .andExpect(jsonPath("$.message").value("제목을 입력해주세요."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());

        assertEquals(0L, lettersRepository.count());
    }

    @Test
    @DisplayName("/api/letters 요청 시 content 값은 필수다.")
    void required_content_value() throws Exception {
        //given
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(null)
                .parentLetterId(null)
                .title("제목입니다!")
                .content("")
                .category(Category.CONSULT)
                .paperType(PaperType.COMFORT)
                .font(FontType.HIMCHAN)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/letters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COM-001"))
                .andExpect(jsonPath("$.message").value("내용을 입력해주세요."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());

        assertEquals(0L, lettersRepository.count());
    }

    @Test
    @DisplayName("/api/v1/letters/{letterId}/previous 요청 시 성공 테스트 - 이전 편지 내용 확인 ")
    void getPreviousLetters_success() throws Exception {
            //given
        CreateLetterRequest request = CreateLetterRequest.builder()
                .receiverId(null)
                .parentLetterId(null)
                .title("제목입니다!")
                .content("내용입니다!")
                .category(Category.CONSULT)
                .paperType(PaperType.COMFORT)
                .font(FontType.HIMCHAN)
                .build();

        LetterResponse letter = letterService.createLetter(request);
        Long letterId = letter.getLetterId(); //생성된 편지 ID

        //expected -> 편지에 대한 답장
        mockMvc.perform(get("/api/v1/letters/{letterId}/previous", letterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                // 여기서는 답장 편지 목록에 방금 생성한 답장 편지만 있으므로 크기는 1이어야 함
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].letterId").value(letterId))
                .andExpect(jsonPath("$.data[0].title").value("제목입니다!"))
                .andExpect(jsonPath("$.data[0].content").value("내용입니다!"))
                .andDo(print());
    }


    @Test
    @DisplayName("소프트 딜리트 테스트 - 편지 삭제 후 isActive가 false로 변경되어야 한다.")
    void delete_softDelete_success() throws Exception {
        // Given: 새로운 편지를 생성하여 저장
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

        // 보통 빌더에서 isActive의 기본값을 true로 설정합니다.
        lettersRepository.save(letter);
        Long letterId = letter.getId();

        assertEquals(true, letter.getIsActive());
        assertTrue(letter.getIsActive(), "편지는 기본적으로 활성 상태여야 함.");

        // When: delete 메서드를 호출하여 소프트 딜리트 수행
        letterService.deleteLetter(letterId);

        // Then: 해당 편지를 다시 조회하면 isActive가 false로 변경되어 있어야 함
        Letter deletedLetter = lettersRepository.findById(letterId).orElseThrow(LetterNotFoundException::new);
        assertFalse(deletedLetter.getIsActive(), "편지 삭제 후 isActive는 false.");
    }

}