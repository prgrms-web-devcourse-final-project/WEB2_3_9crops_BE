package io.crops.warmletter.domain.report.service;

import io.crops.warmletter.domain.report.enums.ReasonType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportModerationService {

    @Value("${google.api.url}")
    private String apiUrl;

    public Map<String, String> moderateText(String text, ReasonType reasonType, String reason) {
        HttpHeaders headers = createHeaders();
        String prompt = buildPrompt(text, reasonType, reason);
        Map<String, Object> requestBody = buildRequestBody(prompt);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = new RestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            log.info("AI Studio 응답: {}", response.getBody());
            Map<String, String> result = parseResponse(response.getBody());
            if (result != null) {
                return result;
            }
        }
        return defaultResult("AI 응답 없음 또는 오류 발생");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String buildPrompt(String text, ReasonType reasonType, String reason) {
        String translatedReasonType = switch (reasonType) {
            case ABUSE -> "욕설";
            case DEFAMATION -> "비방";
            case HARASSMENT -> "성희롱";
            case THREATS -> "폭언";
            case ETC -> "기타";
        };

        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 신고 내용을 보고, 'PENDING' 또는 'RESOLVED' 중 하나만 답변하세요.\n")
                .append("PENDING: 신고된 내용이 서비스 약관을 위반하지 않음 (관리자 검토 필요)\n")
                .append("RESOLVED: 신고된 내용이 명백히 서비스 약관을 위반함 (즉시 처리)\n\n")
                .append("신고 유형: ").append(translatedReasonType).append("\n")
                .append("신고 내용: ").append(text).append("\n");
        if (reason != null && !reason.isBlank()) {
            prompt.append("사용자 설명: ").append(reason).append("\n");
        }
        prompt.append("이제 신고를 판단하고 'PENDING' 또는 'RESOLVED' 중 하나만 출력하세요.");
        return prompt.toString();
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        body.put("generationConfig", Map.of("temperature", 0.3));
        return body;
    }

    private Map<String, String> parseResponse(Map responseBody) {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts != null && !parts.isEmpty()) {
                Object rawResponse = parts.get(0).get("text");
                if (rawResponse instanceof String) {
                    String aiResponse = ((String) rawResponse).trim();
                    log.info("AI 응답: {}", aiResponse);
                    String status = "RESOLVED".equalsIgnoreCase(aiResponse) ? "RESOLVED" : "PENDING";
                    Map<String, String> result = new HashMap<>();
                    result.put("status", status);
                    result.put("adminMemo", "AI 검열 결과:\n" + aiResponse);
                    return result;
                }
            }
        }
        return null;
    }

    private Map<String, String> defaultResult(String memo) {
        Map<String, String> result = new HashMap<>();
        result.put("status", "PENDING");
        result.put("adminMemo", memo);
        return result;
    }
}
