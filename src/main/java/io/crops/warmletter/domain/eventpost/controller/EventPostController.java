package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostStatusResponse;
import io.crops.warmletter.domain.eventpost.service.EventCommentService;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "이벤트 게시판 기능 API", description = "이벤트 게시판 생성, 삭제, 조회 등 이벤트 게시판 관련 기능의 API를 제공합니다.")
public class EventPostController {
    private final EventPostService eventPostService;

    @PostMapping("/admin/event-posts")
    @Operation(summary = "이벤트 게시판 생성", description = "미사용인 새로운 이벤트 게시판을 생성합니다.")
    public ResponseEntity<BaseResponse<EventPostResponse>> createEventPost(@RequestBody @Valid CreateEventPostRequest createEventPostRequest){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.createEventPost(createEventPostRequest),"게시판 생성 성공"));
    }

    @DeleteMapping("/admin/event-posts/{eventPostId}")
    @Operation(summary = "이벤트 게시판 삭제", description = "특정 이벤트 게시판을 삭제합니다.(임시 기능)")
    public ResponseEntity<BaseResponse<Map<String,Long>>> deleteEventPost(@PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.deleteEventPost(eventPostId),"게시판 삭제 성공"));
    }

    @GetMapping("/event-posts")
    @Operation(summary = "사용중인 이벤트 게시판 조회", description = "현재 사용 중인 이벤트 게시판을 조회합니다.")
    public ResponseEntity<BaseResponse<EventPostResponse>> getUsedEventPost(){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.getUsedEventPost(),"게시판 조회(사용중) 성공"));
    }

    @GetMapping("/event-posts/{eventPostId}")
    @Operation(summary = "개별 이벤트 게시판 조회", description = "특정 이벤트 게시판의 세부 정보(제목, 댓글들)를 조회합니다.")
    public ResponseEntity<BaseResponse<EventPostDetailResponse>> getEventPostDetail(@PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.getEventPostDetail(eventPostId),"게시판 조회(개별) 성공"));
    }

    @PatchMapping("/admin/event-posts/{eventPostId}/status")
    @Operation(summary = "이벤트 게시판 사용여부 변경", description = "이벤트 게시판의 사용 여부를 변경하며, 사용중인 게시판은 하나만 적용됩니다. (사용중(true) <-> 미사용(false))")
    public ResponseEntity<BaseResponse<EventPostStatusResponse>> updateEventPostIsUsed(@PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.updateEventPostIsUsed(eventPostId),"게시판 사용여부 변경 성공"));
    }

}
