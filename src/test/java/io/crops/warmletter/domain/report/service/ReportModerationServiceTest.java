package io.crops.warmletter.domain.report.service;

import io.crops.warmletter.domain.report.enums.ReasonType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import org.springframework.test.util.ReflectionTestUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportModerationServiceTest {
    @MockitoBean
    private ReportService reportService;

    private final ReportModerationService service = new ReportModerationService();

    {
        // apiUrl 필드를 강제로 설정 (application.yml 없이 테스트할 때)
        ReflectionTestUtils.setField(service, "apiUrl", "http://fakeapi");
    }



    @Test
    @DisplayName("moderateText 테스트: RESOLVED 응답")
    public void testModerateTextPending() {
        // "PENDING"로 처리되는 응답 (RESOLVED가 아닌 텍스트)
        Map<String, Object> fakeContent = new HashMap<>();
        fakeContent.put("parts", List.of(Map.of("text", "not resolved")));
        Map<String, Object> candidate = new HashMap<>();
        candidate.put("content", fakeContent);
        Map<String, Object> fakeResponseBody = new HashMap<>();
        fakeResponseBody.put("candidates", List.of(candidate));

        ResponseEntity<Map> fakeResponseEntity = new ResponseEntity<>(fakeResponseBody, HttpStatus.OK);

        try (MockedConstruction<RestTemplate> mocked =
                     mockConstruction(RestTemplate.class, (mock, context) -> {
                         when(mock.exchange(eq("http://fakeapi"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                                 .thenReturn(fakeResponseEntity);
                     })) {
            Map<String, String> result = service.moderateText("test text", ReasonType.HARASSMENT, "reason");
            assertNotNull(result);
            assertEquals("PENDING", result.get("status"));
            assertTrue(result.get("adminMemo").contains("not resolved"));
        }
    }

    @Test
    @DisplayName("moderateText 테스트: PENDING 응답")
    public void testModerateTextNoCandidates() {
        // 후보 목록이 빈 경우(default 결과 반환)
        Map<String, Object> fakeResponseBody = new HashMap<>();
        fakeResponseBody.put("candidates", List.of());
        ResponseEntity<Map> fakeResponseEntity = new ResponseEntity<>(fakeResponseBody, HttpStatus.OK);

        try (MockedConstruction<RestTemplate> mocked =
                     mockConstruction(RestTemplate.class, (mock, context) -> {
                         when(mock.exchange(eq("http://fakeapi"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                                 .thenReturn(fakeResponseEntity);
                     })) {
            Map<String, String> result = service.moderateText("text", ReasonType.DEFAMATION, "");
            assertNotNull(result);
            assertEquals("PENDING", result.get("status"));
            assertTrue(result.get("adminMemo").contains("AI 응답 없음 또는 오류 발생"));
        }
    }

    @Test
    @DisplayName("moderateText 테스트: 기본 결과 반환")
    public void testModerateTextResponseNotOk() {
        // 응답 상태가 OK가 아닌 경우
        ResponseEntity<Map> fakeResponseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        try (MockedConstruction<RestTemplate> mocked =
                     mockConstruction(RestTemplate.class, (mock, context) -> {
                         when(mock.exchange(eq("http://fakeapi"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                                 .thenReturn(fakeResponseEntity);
                     })) {
            Map<String, String> result = service.moderateText("text", ReasonType.ETC, "reason");
            assertNotNull(result);
            assertEquals("PENDING", result.get("status"));
            assertTrue(result.get("adminMemo").contains("AI 응답 없음 또는 오류 발생"));
        }
    }

    @Test
    @DisplayName("buildPrompt 테스트: 번역 및 내용 확인")
    public void testBuildPrompt() {
        String prompt = (String) ReflectionTestUtils.invokeMethod(
                service, "buildPrompt", "Test Report", ReasonType.THREATS, "Additional reason"
        );
        assertTrue(prompt.contains("폭언"));           // THREATS는 "폭언"으로 번역됨
        assertTrue(prompt.contains("Test Report"));
        assertTrue(prompt.contains("Additional reason"));
        assertTrue(prompt.contains("PENDING") && prompt.contains("RESOLVED"));
    }

    @Test
    @DisplayName("moderateText 테스트: 응답 본문이 null인 경우")
    void testModerateText_NullBody() {
        ResponseEntity<Map> fakeResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.exchange(eq("http://fakeapi"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                            .thenReturn(fakeResponseEntity);
                })) {
            Map<String, String> result = service.moderateText("test", ReasonType.ETC, "reason");
            assertNotNull(result);
            assertEquals("PENDING", result.get("status"));
            assertTrue(result.get("adminMemo").contains("AI 응답 없음 또는 오류 발생"));
        }
    }

    @Test
    @DisplayName("moderateText 테스트: 후보 있음, parts 빈 경우")
    void testModerateText_EmptyParts() {
        Map<String, Object> fakeContent = new HashMap<>();
        fakeContent.put("parts", List.of()); // parts empty
        Map<String, Object> candidate = new HashMap<>();
        candidate.put("content", fakeContent);
        Map<String, Object> fakeResponseBody = new HashMap<>();
        fakeResponseBody.put("candidates", List.of(candidate));
        ResponseEntity<Map> fakeResponseEntity = new ResponseEntity<>(fakeResponseBody, HttpStatus.OK);

        try (MockedConstruction<RestTemplate> mocked =
                     mockConstruction(RestTemplate.class, (mock, context) -> {
                         when(mock.exchange(eq("http://fakeapi"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                                 .thenReturn(fakeResponseEntity);
                     })) {
            Map<String, String> result = service.moderateText("test", ReasonType.ABUSE, "reason");
            assertNotNull(result);
            assertEquals("PENDING", result.get("status"));
            assertTrue(result.get("adminMemo").contains("AI 응답 없음 또는 오류 발생"));
        }
    }

    @Test
    @DisplayName("moderateText 테스트: rawResponse가 문자열이 아닌 경우")
    void testModerateText_RawResponseNotString() {
        Map<String, Object> fakeContent = new HashMap<>();
        // parts에 문자열이 아닌 값을 넣습니다.
        fakeContent.put("parts", List.of(Map.of("text", 123)));
        Map<String, Object> candidate = new HashMap<>();
        candidate.put("content", fakeContent);
        Map<String, Object> fakeResponseBody = new HashMap<>();
        fakeResponseBody.put("candidates", List.of(candidate));
        ResponseEntity<Map> fakeResponseEntity = new ResponseEntity<>(fakeResponseBody, HttpStatus.OK);

        try (MockedConstruction<RestTemplate> mocked =
                     mockConstruction(RestTemplate.class, (mock, context) -> {
                         when(mock.exchange(eq("http://fakeapi"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                                 .thenReturn(fakeResponseEntity);
                     })) {
            Map<String, String> result = service.moderateText("test", ReasonType.ETC, "reason");
            assertNotNull(result);
            assertEquals("PENDING", result.get("status"));
            assertTrue(result.get("adminMemo").contains("AI 응답 없음 또는 오류 발생"));
        }
    }




}
