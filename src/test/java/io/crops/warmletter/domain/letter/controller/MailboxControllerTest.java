package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.service.MailBoxService;
import io.crops.warmletter.global.response.BaseResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(MailboxController.class)
@AutoConfigureMockMvc(addFilters = false)
class MailboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MailBoxService mailBoxService;

    @Test
    @DisplayName("GET /api/mailbox - 내 편지함 목록 조회 성공 테스트")
    void getMailbox_success() throws Exception {
        // given: MailBoxService에서 반환할 더미 데이터 생성
        List<MailboxResponse> mailboxResponses = List.of(
                MailboxResponse.builder()
                        .letterMatchingId(1L)
                        .oppositeZipCode("1A2A3")
                        .isActive(true)
                        .isOppositeRead(false)
                        .build(),
                MailboxResponse.builder()
                        .letterMatchingId(2L)
                        .oppositeZipCode("33DDD")
                        .isActive(true)
                        .isOppositeRead(true)
                        .build(),
                MailboxResponse.builder()
                        .letterMatchingId(3L)
                        .oppositeZipCode("483FZ")
                        .isActive(true)
                        .isOppositeRead(true)
                        .build(),
                MailboxResponse.builder()
                        .letterMatchingId(4L)
                        .oppositeZipCode("33FFF")
                        .isActive(false)
                        .isOppositeRead(true)
                        .build()
        );
        when(mailBoxService.getMailbox()).thenReturn(mailboxResponses);

        // when & then: GET /api/mailbox 요청 후 JSON 응답 검증
        mockMvc.perform(get("/api/mailbox")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].letterMatchingId").value(1))
                .andExpect(jsonPath("$.data[0].oppositeZipCode").value("1A2A3"))
                .andExpect(jsonPath("$.data[0].active").value(true))
                .andExpect(jsonPath("$.data[0].oppositeRead").value(false))
                .andExpect(jsonPath("$.data[1].letterMatchingId").value(2))
                .andExpect(jsonPath("$.data[1].oppositeZipCode").value("33DDD"))
                .andExpect(jsonPath("$.data[1].active").value(true))
                .andExpect(jsonPath("$.data[1].oppositeRead").value(true))
                .andExpect(jsonPath("$.data[2].letterMatchingId").value(3))
                .andExpect(jsonPath("$.data[2].oppositeZipCode").value("483FZ"))
                .andExpect(jsonPath("$.data[2].active").value(true))
                .andExpect(jsonPath("$.data[2].oppositeRead").value(true))
                .andExpect(jsonPath("$.data[3].letterMatchingId").value(4))
                .andExpect(jsonPath("$.data[3].oppositeZipCode").value("33FFF"))
                .andExpect(jsonPath("$.data[3].active").value(false))
                .andExpect(jsonPath("$.data[3].oppositeRead").value(true))
                .andExpect(jsonPath("$.message").value("편지함 조회 완료"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }
}