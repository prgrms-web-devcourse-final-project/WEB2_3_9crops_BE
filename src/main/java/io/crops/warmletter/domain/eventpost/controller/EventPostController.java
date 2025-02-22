package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostResponse;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventPostController {
    private final EventPostService eventPostService;

    // 이벤트 게시판 생성
    @PostMapping("/admin/event-posts")
    public ResponseEntity<BaseResponse<EventPostResponse>> createEventPost(@RequestBody @Valid CreateEventPostRequest createEventPostRequest){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.createEventPost(createEventPostRequest),"이벤트 게시판 생성 완료"));
    }

    // 이벤트 게시판 삭제(임시,미정)
    @DeleteMapping("/admin/event-posts/{eventPostId}")
    public ResponseEntity<BaseResponse<Map<String,Long>>> deleteEventPost(@PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.deleteEventPost(eventPostId),"이벤트 게시판 삭제 완료"));
    }

    @GetMapping("/event-posts")
    public ResponseEntity<BaseResponse<EventPostResponse>> getUsedEventPost(){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.getUsedEventPost(),"이벤트 게시판 조회(사용중) 완료"));
    }

    @GetMapping("/event-posts/{eventPostId}")
    public ResponseEntity<BaseResponse<EventPostDetailResponse>> getEventPostDetail(@PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.getEventPostDetail(eventPostId),"이벤트 게시판 조회(개별) 완료"));
    }
}
