package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.service.LetterMatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LetterMatchingController.class)
@AutoConfigureMockMvc(addFilters = false)
class LetterMatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LetterMatchingService letterMatchingService;


    @Test
    @DisplayName("GET /api/random/{category} 랜덤 편지 리스트 확인")
    void getRandomLetter() throws Exception {
        //given
        Category category = Category.CELEBRATION;
        List<RandomLetterResponse> randomLetters = List.of(
                RandomLetterResponse.builder()
                        .letterId(1L)
                        .content("편지 내용 1")
                        .zipCode("1A2A3")
                        .category(Category.CONSOLATION)
                        .paperType(PaperType.PAPER)
                        .fontType(FontType.KYOBO)
                        .createdAt(LocalDateTime.now())
                        .build(),
                RandomLetterResponse.builder()
                        .letterId(2L)
                        .content("편지 내용 2")
                        .zipCode("33DDD")
                        .category(Category.CONSOLATION)
                        .paperType(PaperType.PAPER)
                        .fontType(FontType.KYOBO)
                        .createdAt(LocalDateTime.now())
                        .build()

        );
        when(letterMatchingService.findRandomLetters(category)).thenReturn(randomLetters);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/random/" + category)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].letterId").value(1L))
                .andExpect(jsonPath("$.data[0].content").value("편지 내용 1"))
                .andExpect(jsonPath("$.data[0].zipCode").value("1A2A3"))
                .andExpect(jsonPath("$.data[0].category").value("CONSOLATION"))
                .andDo(print());
    }

    @Test
    @DisplayName("Post /api/random-letters/valid  -  최종 매칭이 시간 1시간이내일 경우 확인")
    void validRandomLetter() throws Exception {
        CheckLastMatchResponse response = CheckLastMatchResponse.builder()
                .canSend(true)
                .build();

        when(letterMatchingService.checkLastMatched()).thenReturn(response);

        mockMvc.perform(post("/api/random-letters/valid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("새 편지 매칭까지 남은 시간"))
                .andExpect(jsonPath("$.data.canSend").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("Post /api/random-letters/valid-table  - 임시테이블에 내 데이터 없을 경우")
    void temporaryTableEmptyReturnsFalse() throws Exception {

        TemporaryMatchingResponse response = TemporaryMatchingResponse.builder()
                .isTemporary(false)
                .build();

        when(letterMatchingService.checkTemporaryMatchedTable()).thenReturn(response);

        mockMvc.perform(post("/api/random-letters/valid-table")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.temporary").value(false)).andDo(print());
    }
    @Test
    @DisplayName("Post /api/random-letters/valid-table  - 임시테이블에 내 데이터 있을 경우")
    void temporaryTableHasDataReturnsTrue() throws Exception {
        TemporaryMatchingResponse response = TemporaryMatchingResponse.builder()
                .letterId(1L)
                .letterTitle("두번째 편지 작성")
                .content("내용")
                .zipCode("12345")
                .category(Category.CELEBRATION)
                .paperType(PaperType.PAPER)
                .fontType(FontType.KYOBO)
                .createdAt(LocalDateTime.now())
                .replyDeadLine(LocalDateTime.now().plusDays(1))
                .isTemporary(true)
                .build();

        when(letterMatchingService.checkTemporaryMatchedTable()).thenReturn(response);

        mockMvc.perform(post("/api/random-letters/valid-table")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterId").value(1L))
                .andExpect(jsonPath("$.data.letterTitle").value("두번째 편지 작성"))
                .andExpect(jsonPath("$.data.temporary").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("Delete /api/random-letters/matching/cancel  - 매칭취소")
        void matchingCancel() throws Exception {

        mockMvc.perform(delete("/api/random-letters/matching/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value("매칭 취소 성공"))
                .andExpect(jsonPath("$.message").value("랜덤 편지 매칭이 취소되었습니다."))
                .andExpect(status().isOk())
                .andDo(print());
        }
    }