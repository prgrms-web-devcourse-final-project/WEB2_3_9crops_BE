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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
}
