package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.service.SharePostService;
import io.crops.warmletter.global.response.BaseResponse;
import io.crops.warmletter.global.response.PageResponse;
import io.crops.warmletter.global.util.PageableConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SharePostController {

    private final SharePostService sharePostService;

    @GetMapping("/share-posts")
    public ResponseEntity<PageResponse<SharePostResponse>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(new PageResponse<>(sharePostService.getAllPosts(PageableConverter.convertToPageable(pageable))));
    }

    @GetMapping("/share-posts/{sharePostId}")
    public ResponseEntity<BaseResponse<SharePostDetailResponse>> getPostDetail(@PathVariable(name = "sharePostId") Long sharePostId)
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(sharePostService.getPostDetail(sharePostId),"성공"));
    }



}

