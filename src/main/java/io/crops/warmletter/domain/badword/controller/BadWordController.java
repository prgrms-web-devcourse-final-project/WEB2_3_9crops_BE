package io.crops.warmletter.domain.badword.controller;


import io.crops.warmletter.domain.badword.dto.request.BadWordRequest;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bad-word")
@RequiredArgsConstructor
public class BadWordController {


    private final BadWordService badWordService;

    @PostMapping
    public ResponseEntity<BaseResponse<String>> createModerationWord(@RequestBody BadWordRequest request) {
        badWordService.saveModerationWord(request);
        return ResponseEntity.ok(BaseResponse.of(request.getWord(), "성공"));
    }

}
