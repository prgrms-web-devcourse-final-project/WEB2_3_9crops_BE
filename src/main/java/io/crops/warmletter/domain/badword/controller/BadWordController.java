package io.crops.warmletter.domain.badword.controller;


import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bad-words")
@RequiredArgsConstructor
public class BadWordController {


    private final BadWordService badWordService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createBadWord(@RequestBody @Valid CreateBadWordRequest request) {
        badWordService.createBadWord(request);
        return ResponseEntity.ok(BaseResponse.of(null, "검열단어 등록완료"));
    }

}
