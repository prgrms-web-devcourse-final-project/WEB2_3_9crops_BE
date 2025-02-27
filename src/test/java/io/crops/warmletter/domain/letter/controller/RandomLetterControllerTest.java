package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.request.ApproveLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.service.RandomLetterService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RandomLetterController.class)
@AutoConfigureMockMvc(addFilters = false)
class RandomLetterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RandomLetterService randomLetterService;


    @Test
    @DisplayName("GET /api/random-letters/{category} 랜덤 편지 리스트 확인")
    void getRandomLetter() throws Exception {
        //given
        Category category = Category.CELEBRATION;
        List<RandomLetterResponse> randomLetters = List.of(
                RandomLetterResponse.builder()
                        .letterId(1L)
                        .title("편지 제목 1")
                        .zipCode("1A2A3")
                        .category(Category.CONSOLATION)
                        .createdAt(LocalDateTime.now())
                        .build(),
                RandomLetterResponse.builder()
                        .letterId(2L)
                        .title("편지 제목 2")
                        .zipCode("33DDD")
                        .category(Category.CONSOLATION)
                        .createdAt(LocalDateTime.now())
                        .build()

        );
        when(randomLetterService.findRandomLetters(category)).thenReturn(randomLetters);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/random-letters/" + category)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].letterId").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("편지 제목 1"))
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

        when(randomLetterService.checkLastMatched()).thenReturn(response);

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

        when(randomLetterService.checkTemporaryMatchedTable()).thenReturn(response);

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

        when(randomLetterService.checkTemporaryMatchedTable()).thenReturn(response);

        mockMvc.perform(post("/api/random-letters/valid-table")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterId").value(1L))
                .andExpect(jsonPath("$.data.letterTitle").value("두번째 편지 작성"))
                .andExpect(jsonPath("$.data.temporary").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("Delete /api/random-letters/matching/cancel - 매칭취소")
        void matchingCancel() throws Exception {

        doNothing().when(randomLetterService).matchingCancel();

        mockMvc.perform(delete("/api/random-letters/matching/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("랜덤 편지 매칭이 취소되었습니다."))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /api/random-letters/approve - 랜덤 편지 승인")
    void approveLetter() throws Exception {
        // given
        ApproveLetterRequest request = ApproveLetterRequest.builder()
                .letterId(1L)
                .writerId(2L)
                .build();
        doNothing().when(randomLetterService).approveLetter(any(ApproveLetterRequest.class));

        // when,then
        mockMvc.perform(post("/api/random-letters/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("랜덤 편지 승인 완료"))
                .andDo(print());
    }



    }