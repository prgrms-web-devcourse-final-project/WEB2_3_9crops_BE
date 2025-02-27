package io.crops.warmletter.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.letter.dto.request.EvaluateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.TemporarySaveLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.exception.LetterNotBelongException;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.service.LetterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private Letter letter;

    @BeforeEach
    void setUp() {
        letter = Letter.builder()
                .writerId(1L)
                .letterType(LetterType.RANDOM)
                .title("편지 제목")
                .content("편지 답장")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .fontType(FontType.KYOBO)
                .receiverId(2L)
                .parentLetterId(1L)
                .build();
        ReflectionTestUtils.setField(letter, "id", 1L);
    }

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

    @Test
    @DisplayName("GET /api/letters/{letterId} - 편지 조회 성공 테스트")
    void getLetter_success() throws Exception {
        String zipCode = "12345";
        LetterResponse letterResponse = LetterResponse.fromEntityForDetailView(letter, zipCode);

        when(letterService.getLetterById(letter.getId())).thenReturn(letterResponse);

        mockMvc.perform(get("/api/letters/{letterId}", letter.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterId").value(1L))
                .andExpect(jsonPath("$.data.title").value("편지 제목"))
                .andExpect(jsonPath("$.data.content").value("편지 답장"))
                .andExpect(jsonPath("$.data.paperType").value("PAPER"))
                .andExpect(jsonPath("$.data.fontType").value("KYOBO"))
                .andDo(print());
    }

    @DisplayName("편지 평가하기 API 호출 실패 - 권한 없는 편지")
    @Test
    void evaluateLetter_Fail_NotBelongLetter() throws Exception {
        //given
        Long invalidLetterId = 999L;

        EvaluateLetterRequest request = new EvaluateLetterRequest();
        LetterEvaluation evaluation = LetterEvaluation.GOOD;

        Field evaluationField = EvaluateLetterRequest.class.getDeclaredField("evaluation");
        evaluationField.setAccessible(true);
        evaluationField.set(request, evaluation);

        doThrow(new LetterNotBelongException())
                .when(letterService)
                .evaluateLetter(eq(invalidLetterId), any(EvaluateLetterRequest.class));

        //when & then
        mockMvc.perform(post("/api/letters/" + invalidLetterId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("LET-006"))
                .andExpect(jsonPath("$.message").value("편지에 대한 권한이 없습니다."));

        verify(letterService).evaluateLetter(
                eq(invalidLetterId),
                any(EvaluateLetterRequest.class)
        );
    }

    @DisplayName("편지 평가하기 API 호출 성공")
    @Test
    void evaluateLetter_Success() throws Exception {
        //given
        Long letterId = 1L;

        EvaluateLetterRequest request = new EvaluateLetterRequest();
        LetterEvaluation evaluation = LetterEvaluation.GOOD;

        Field evaluationField = EvaluateLetterRequest.class.getDeclaredField("evaluation");
        evaluationField.setAccessible(true);
        evaluationField.set(request, evaluation);

        doNothing()
                .when(letterService)
                .evaluateLetter(eq(letterId), any(EvaluateLetterRequest.class));

        //when & then
        mockMvc.perform(post("/api/letters/" + letterId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("편지 평가 완료"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(letterService).evaluateLetter(
                eq(letterId),
                any(EvaluateLetterRequest.class)
        );
    }
    @Test
    @DisplayName("POST /api/letters/{letterId}/temporary-save - 편지 임시 저장 성공 테스트")
    void temporarySaveLetter_success() throws Exception {
        // given
        Long letterId = 1L;

        TemporarySaveLetterRequest request = new TemporarySaveLetterRequest();
        request.setTitle("임시 저장 제목");
        request.setContent("임시 저장할 내용입니다.");
        request.setCategory(Category.ETC);
        request.setPaperType(PaperType.PAPER);
        request.setFont(FontType.GYEONGGI);

        LetterResponse expectedResponse = LetterResponse.builder()
                .letterId(letterId)
                .writerId(1L)
                .title("임시 저장 제목")
                .content("임시 저장할 내용입니다.")
                .category(Category.ETC)
                .paperType(PaperType.PAPER)
                .fontType(FontType.GYEONGGI)
                .build();

        when(letterService.temporarySaveLetter(eq(letterId), any(TemporarySaveLetterRequest.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/letters/{letterId}/temporary-save", letterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("임시 저장 완료 "))
                .andExpect(jsonPath("$.data.letterId").value(letterId))
                .andExpect(jsonPath("$.data.title").value("임시 저장 제목"))
                .andExpect(jsonPath("$.data.content").value("임시 저장할 내용입니다."))
                .andExpect(jsonPath("$.data.category").value("ETC"))
                .andExpect(jsonPath("$.data.paperType").value("PAPER"))
                .andExpect(jsonPath("$.data.fontType").value("GYEONGGI"))
                .andDo(print());

        verify(letterService).temporarySaveLetter(eq(letterId), any(TemporarySaveLetterRequest.class));
    }



    @Test
    @DisplayName("POST /api/letters/{letterId}/temporary-save - 존재하지 않는 편지 임시 저장 실패 테스트")
    void temporarySaveLetter_fail_letterNotFound() throws Exception {
        // given
        Long nonExistentLetterId = 999L;

        TemporarySaveLetterRequest request = new TemporarySaveLetterRequest();
        request.setTitle("임시 저장 제목");
        request.setContent("임시 저장할 내용입니다.");

        doThrow(new LetterNotFoundException())
                .when(letterService)
                .temporarySaveLetter(eq(nonExistentLetterId), any(TemporarySaveLetterRequest.class));

        // when & then
        mockMvc.perform(post("/api/letters/{letterId}/temporary-save", nonExistentLetterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LET-001"))
                .andExpect(jsonPath("$.message").value("해당 편지를 찾을 수 없습니다."))
                .andDo(print());

        verify(letterService).temporarySaveLetter(
                eq(nonExistentLetterId),
                any(TemporarySaveLetterRequest.class)
        );
    }

    @Test
    @DisplayName("POST /api/letters/{letterId}/temporary-save - 권한 없는 편지 임시 저장 실패 테스트")
    void temporarySaveLetter_fail_notBelongLetter() throws Exception {
        // given
        Long unauthorizedLetterId = 888L;

        TemporarySaveLetterRequest request = new TemporarySaveLetterRequest();
        request.setTitle("임시 저장 제목");
        request.setContent("임시 저장할 내용입니다.");

        doThrow(new LetterNotBelongException())
                .when(letterService)
                .temporarySaveLetter(eq(unauthorizedLetterId), any(TemporarySaveLetterRequest.class));

        // when & then
        mockMvc.perform(post("/api/letters/{letterId}/temporary-save", unauthorizedLetterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("LET-006"))
                .andExpect(jsonPath("$.message").value("편지에 대한 권한이 없습니다."))
                .andDo(print());

        verify(letterService).temporarySaveLetter(
                eq(unauthorizedLetterId),
                any(TemporarySaveLetterRequest.class)
        );
    }
}