package io.crops.warmletter.domain.moderation.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.moderation.dto.request.ModerationRequest;
import io.crops.warmletter.domain.moderation.service.ModerationService;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.doThrow;


import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@AutoConfigureMockMvc(addFilters = false)  // 이거 추가!!
@WebMvcTest(ModerationController.class)
class ModerationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModerationService moderationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("금칙어 등록 API 성공 테스트")
    void createModerationWord_success() throws Exception {
        // given
        ModerationRequest request = new ModerationRequest("금칙어");

        // when & then
        mockMvc.perform(post("/api/moderations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("검열단어 등록완료"))
                .andExpect(jsonPath("$.message").value("성공"));

        Mockito.verify(moderationService).saveModerationWord(any(ModerationRequest.class));
    }

    @Test
    @DisplayName("금칙어 등록 API 실패 테스트 - 중복 단어")
    void createModerationWord_fail_duplicate() throws Exception {
        // given
        ModerationRequest request = new ModerationRequest("금칙어");

        doThrow(new BusinessException(ErrorCode.DUPLICATE_BANNED_WORD))
                .when(moderationService).saveModerationWord(any(ModerationRequest.class));

        // when & then
        mockMvc.perform(post("/api/moderations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.DUPLICATE_BANNED_WORD.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_BANNED_WORD.getMessage()));
    }
}
