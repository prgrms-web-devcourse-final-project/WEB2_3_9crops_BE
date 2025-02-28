package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.service.SharePostService;
import io.crops.warmletter.global.response.BaseResponse;
import io.crops.warmletter.global.response.PageResponse;
import io.crops.warmletter.global.util.PageableConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "SharePost",description = "공유 게시판 관련 API")
public class SharePostController {

    private final SharePostService sharePostService;

    @Operation(summary = "공유 게시글 목록 조회", description = "페이징 처리된 공유 게시글 목록을 조회합니다.")
    @GetMapping("/share-posts")
    public ResponseEntity<BaseResponse<PageResponse<SharePostResponse>>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.of(new PageResponse<>(sharePostService.getAllPosts(PageableConverter.convertToPageable(pageable))), "공유 게시글 조회 성공"));
    }
    
    @Operation(summary = "공유 게시글 상세 조회", description = "특정 ID의 공유 게시글 상세 정보를 조회합니다.")
    @GetMapping("/share-posts/{sharePostId}")
    public ResponseEntity<BaseResponse<SharePostDetailResponse>> getPostDetail(@PathVariable(name = "sharePostId") Long sharePostId)
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(sharePostService.getPostDetail(sharePostId),"성공"));
    }



}
