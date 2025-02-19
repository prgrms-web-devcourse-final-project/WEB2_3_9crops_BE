package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.service.SharePostService;
import io.crops.warmletter.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SharePostController {

    private final SharePostService sharePostService;

    @GetMapping("/share-posts")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PageResponse<SharePostResponse>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
//            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new PageResponse<>(sharePostService.getAllPosts(pageable)));
    }
}
