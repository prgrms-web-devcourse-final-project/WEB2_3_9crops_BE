package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.service.LetterMatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
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
        String category = "CONSOLATION";
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

        mockMvc.perform(MockMvcRequestBuilders.get("/api/random/" + category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(randomLetters.size()))
                .andExpect(jsonPath("$.data[0].letterId").value(1L))
                .andExpect(jsonPath("$.data[0].content").value("편지 내용 1"))
                .andExpect(jsonPath("$.data[0].zipCode").value("1A2A3"))
                .andExpect(jsonPath("$.data[0].category").value(category))
                .andExpect(jsonPath("$.message").value("랜덤편지 조회 완료"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }




}