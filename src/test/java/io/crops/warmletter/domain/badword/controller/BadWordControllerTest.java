package io.crops.warmletter.domain.badword.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import io.crops.warmletter.domain.badword.service.BadWordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(TestConfig.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BadWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BadWordRepository badWordRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        badWordRepository.deleteAll(); // 테스트 전 초기화
    }

    @Test
    @DisplayName("검열 단어 등록 성공")
    void createBadWord_Success() throws Exception {
        CreateBadWordRequest request = new CreateBadWordRequest("badword");


        mockMvc.perform(post("/api/bad-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("검열단어 등록완료"));
    }


    @Test
    @DisplayName("검열 단어 등록 실패 - 빈값일 때")
    void createBadWord_EmptyValue_Fail() throws Exception {
        CreateBadWordRequest request = new CreateBadWordRequest("");

        mockMvc.perform(post("/api/bad-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("단어는 필수 입력값입니다."));

    }


    @Test
    @DisplayName("검열 단어 중복 등록 실패")
    void createBadWord_Duplicate() throws Exception {
        // Given: 먼저 단어를 저장해놓고 중복 요청
        badWordRepository.save(BadWord.builder()
                .word("badword")
                .isUsed(true)
                .build());

        CreateBadWordRequest request = new CreateBadWordRequest("badword");

        mockMvc.perform(post("/api/bad-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())  // 409 상태 코드로 수정
                .andExpect(jsonPath("$.message").value("이미 등록된 금칙어입니다."));  // 메시지 검증
    }

}
