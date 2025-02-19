package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class LettersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LetterRepository lettersRepository;

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
                .category(Category.SAD)
                .paperType(PaperType.TYPE_A)
                .font(FontType.BOLD)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/letters")
                .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(print());

        //then
        assertEquals(1L, lettersRepository.count());

//        Letter letter = lettersRepository.findAll().get(0);
//        assertEquals("제목입니다", letter.getTitle());
//        assertEquals("편지 내용입니다", letter.getContent());
//        assertEquals(Category.SAD, letter.getCategory());
//        assertEquals(PaperType.TYPE_A, letter.getPaperType());
//        assertEquals(FontType.BOLD, letter.getFontType());
        
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
                .category(Category.SAD)
                .paperType(PaperType.TYPE_A)
                .font(FontType.BOLD)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/letters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(print());

        //then
        assertEquals(1L, lettersRepository.count());

//        Letter letter = lettersRepository.findAll().get(0);
//        assertEquals("제목입니다", letter.getTitle());
//        assertEquals("편지 내용입니다", letter.getContent());
//        assertEquals(Category.SAD, letter.getCategory());
//        assertEquals(PaperType.TYPE_A, letter.getPaperType());
//        assertEquals(FontType.BOLD, letter.getFontType());
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
                .category(Category.SAD)
                .paperType(PaperType.TYPE_A)
                .font(FontType.BOLD)
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
                .category(Category.SAD)
                .paperType(PaperType.TYPE_A)
                .font(FontType.BOLD)
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

}