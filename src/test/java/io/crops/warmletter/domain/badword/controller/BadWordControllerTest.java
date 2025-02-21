package io.crops.warmletter.domain.badword.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.service.BadWordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.when;





@Import(TestConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class BadWordControllerTest {

    @Autowired
    private MockMvc mockMvc; //이부분은 주입을받아야함


    @MockitoBean
    private BadWordService badWordService;


    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("금칙어 등록 성공")
    void createBadWord_Success() throws Exception {
        CreateBadWordRequest request = new CreateBadWordRequest("씹새끼");


        mockMvc.perform(post("/api/bad-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("금칙어 등록완료"));
    }


    @Test
    @DisplayName("금칙어 실패 - 빈값일 때")
    void createBadWord_EmptyValue_Fail() throws Exception {
        CreateBadWordRequest request = new CreateBadWordRequest("");

        mockMvc.perform(post("/api/bad-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("단어는 필수 입력값입니다."));

    }

    @Test
    @DisplayName("금칙어 상태 업데이트 성공")
    void updateBadWordStatus_Success() throws Exception {
        UpdateBadWordStatusRequest request = new UpdateBadWordStatusRequest(true);

        mockMvc.perform(patch("/api/bad-words/{badWordId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("금칙어 상태 변경 완료"));
    }

    @Test
    @DisplayName("GET /badwords - 금칙어 조회 API 정상 응답 확인")
    void getBadWords_ReturnsCorrectResponse() throws Exception {
        // given: 목 서비스의 반환값 설정
        Map<String, String> badWord1 = new HashMap<>();
        badWord1.put("id", "1");
        badWord1.put("word", "시발");

        Map<String, String> badWord2 = new HashMap<>();
        badWord2.put("id", "2");
        badWord2.put("word", "병신");

        List<Map<String, String>> mockBadWords = List.of(badWord1, badWord2);
        when(badWordService.getBadWords()).thenReturn(mockBadWords);

        // when & then: GET 요청 후 응답 JSON 구조 및 값 검증
        mockMvc.perform(get("/api/bad-words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].word").value("시발"))
                .andExpect(jsonPath("$.data[1].id").value("2"))
                .andExpect(jsonPath("$.data[1].word").value("병신"))
                .andExpect(jsonPath("$.message").value("금칙어 조회"));
    }


}
