package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.response.MailboxDetailResponse;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.exception.MatchingAlreadyBlockedException;
import io.crops.warmletter.domain.letter.exception.MatchingNotBelongException;
import io.crops.warmletter.domain.letter.exception.MatchingNotFoundException;
import io.crops.warmletter.domain.letter.service.MailboxService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private MailboxService mailBoxService;

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
    
    @DisplayName("매칭 차단 API 호출 실패 - 존재하지 않은 매칭")
    @Test
    void disconnectMatching_Fail_NotFoundMatching() throws Exception {
        //given
        Long invalidMatchingId = 1L;

        doThrow(new MatchingNotFoundException())
                .when(mailBoxService)
                .disconnectMatching(invalidMatchingId);

        //when & then
        mockMvc.perform(post("/api/mailbox/" + invalidMatchingId + "/disconnect"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LET-003"))
                .andExpect(jsonPath("$.message").value("매칭을 찾을 수 없습니다."));

        verify(mailBoxService).disconnectMatching(invalidMatchingId);
    }

    @DisplayName("매칭 차단 API 호출 실패 - 속하지 않은 매칭")
    @Test
    void disconnectMatching_Fail_NotBelong() throws Exception {
        //given
        Long invalidMatchingId = 1L;

        doThrow(new MatchingNotBelongException())
                .when(mailBoxService)
                .disconnectMatching(invalidMatchingId);

        //when & then
        mockMvc.perform(post("/api/mailbox/" + invalidMatchingId + "/disconnect"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("LET-004"))
                .andExpect(jsonPath("$.message").value("해당 매칭에 대해 권한이 없습니다."));

        verify(mailBoxService).disconnectMatching(invalidMatchingId);
    }

    @DisplayName("매칭 차단 API 호출 실패 - 이미 차단된 매칭")
    @Test
    void disconnectMatching_Fail_AlreadyBlockedMatching() throws Exception {
        //given
        Long invalidMatchingId = 1L;

        doThrow(new MatchingAlreadyBlockedException())
                .when(mailBoxService)
                .disconnectMatching(invalidMatchingId);

        //when & then
        mockMvc.perform(post("/api/mailbox/" + invalidMatchingId + "/disconnect"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LET-005"))
                .andExpect(jsonPath("$.message").value("이미 매칭이 차단되었습니다."));

        verify(mailBoxService).disconnectMatching(invalidMatchingId);
    }

    @DisplayName("매칭 차단 API 호출 성공")
    @Test
    void disconnectMatching_Success() throws Exception {
        //given
        Long validMatchingId = 1L;

        doNothing().when(mailBoxService).disconnectMatching(validMatchingId);

        //when & then
        mockMvc.perform(post("/api/mailbox/" + validMatchingId + "/disconnect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("매칭 차단 완료"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(mailBoxService).disconnectMatching(validMatchingId);
    }

    @Test
    @DisplayName("GET /api/mailbox/{matchingId}/detail - 간단한 편지함 상세 조회 테스트")
    void detailMailbox_simple() throws Exception {
        Long matchingId = 1L;

        MailboxDetailResponse detailResponse = MailboxDetailResponse.builder()
                .letterId(10L)
                .title("내용입니다~~")
                .myLetter(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").descending());
        Page<MailboxDetailResponse> page = new PageImpl<>(List.of(detailResponse), pageable, 1);

        when(mailBoxService.detailMailbox(eq(matchingId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/mailbox/{matchingId}/detail", matchingId)
                        .param("page", "1")  // 클라이언트에서 1페이지로 요청 (컨트롤러에서 0으로 조정됨~!~!~)
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("편지함 상세 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].letterId").value(10))
                .andExpect(jsonPath("$.data.content[0].title").value("내용입니다~~"))
                .andDo(print());

    }


    @Test
    @DisplayName("GET /api/mailbox/{matchingId}/detail - 페이지 번호가 음수인 경우 테스트")
    void detailMailbox_withNegativePage() throws Exception {
        // given: 페이지 번호가 음수인 경우의 테스트 데이터 생성
        Long matchingId = 1L;

        MailboxDetailResponse detailResponse = MailboxDetailResponse.builder()
                .letterId(10L)
                .title("내용입니다~~")
                .myLetter(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        // ArgumentCaptor를 사용하여 실제로 서비스에 전달된 Pageable 객체를 검증
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by("id").descending());
        Page<MailboxDetailResponse> page = new PageImpl<>(List.of(detailResponse), expectedPageable, 1);

        when(mailBoxService.detailMailbox(eq(matchingId), pageableCaptor.capture())).thenReturn(page);

        // when & then: GET 요청 수행 및 검증 (page=-1 파라미터 전달)
        mockMvc.perform(get("/api/mailbox/{matchingId}/detail", matchingId)
                        .param("page", "-1")  // 음수 페이지 번호 전달
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("편지함 상세 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].letterId").value(10))
                .andExpect(jsonPath("$.data.content[0].title").value("내용입니다~~"))
                .andDo(print());

        // 서비스에 전달된 Pageable 객체 검증
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());
        assertEquals(Sort.by("id").descending(), capturedPageable.getSort());
    }

}