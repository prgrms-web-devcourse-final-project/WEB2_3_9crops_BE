package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.CreateEventPostResponse;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventPostController {
    private final EventPostService eventPostService;

    @PostMapping("/admin/event-posts")
    public ResponseEntity<BaseResponse<CreateEventPostResponse>> createEventPost(@RequestBody CreateEventPostRequest createEventPostRequest){
        CreateEventPostResponse createEventPostResponse = eventPostService.createEventPost(createEventPostRequest);
        return ResponseEntity.ok(BaseResponse.of(createEventPostResponse,"게시판 생성 완료"));
    }
}
