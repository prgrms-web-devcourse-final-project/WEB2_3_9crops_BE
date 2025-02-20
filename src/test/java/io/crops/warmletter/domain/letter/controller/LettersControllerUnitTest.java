package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.service.LetterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LetterController.class)
@AutoConfigureMockMvc(addFilters = false) //MockMvc를 구성할 때 스프링 시큐리티 필터(예: 인증, 권한 검사 등)를 완전 비활성화~~
class LettersControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LetterService letterService;

    @Test
    @DisplayName("GET /api/v1/letters/{letterId}/previous 단위 테스트 - 성공")
    void getPreviousLetters_success_unit() throws Exception {
        // given 하나의 답장 편지
        List<LetterResponse> letterResponses = List.of(
                LetterResponse.builder()
                        .letterId(1L)
                        .writerId(1L)
                        .title("제목입니다!")
                        .content("내용입니다!")
                        .build()
        );

        //호출하면 위 목록을 반환하도록 설정
        when(letterService.getPreviousLetters(1L)).thenReturn(letterResponses);

        // when & then
        mockMvc.perform(get("/api/v1/letters/{letterId}/previous", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].letterId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("제목입니다!"))
                .andExpect(jsonPath("$.data[0].content").value("내용입니다!"));
    }

    @Test
    @DisplayName("DELETE /api/letters/{letterId} - 편지 삭제 성공 테스트")
    void deleteLetter_success() throws Exception {
        Long letterId = 1L;

        mockMvc.perform(delete("/api/letters/{letterId}", letterId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("편지 삭제 완료"))
                .andDo(print());
    }
}