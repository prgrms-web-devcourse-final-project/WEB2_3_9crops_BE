package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SharePostService {

    private final SharePostRepository sharePostRepository;

    @Transactional(readOnly = true)
    public Page<SharePostResponse> getAllPosts(Pageable pageable) {
        // 0보다 작은 페이지를 요청한 경우
        if (pageable.getPageNumber() < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }
        Page<SharePost> sharePosts = sharePostRepository.findAllByIsActiveTrue(pageable);
        return sharePosts.map(SharePostResponse::new);
    }

    @Transactional(readOnly = true)
    public SharePostDetailResponse getPostDetail(Long sharePostId) {
        return sharePostRepository.findDetailById(sharePostId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND));
    }


}
