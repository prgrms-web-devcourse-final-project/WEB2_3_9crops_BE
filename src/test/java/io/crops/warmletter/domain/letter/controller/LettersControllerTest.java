package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .andExpect(jsonPath("$.data.font").value("KYOBO"))
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
                .andExpect(jsonPath("$.data.font").value("HIMCHAN"))
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

}