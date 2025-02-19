package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.repository.ShareRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SharePostService {

    private final ShareRepository shareRepository;

    @Transactional(readOnly = true)
    public Page<SharePostResponse> getAllPosts(Pageable pageable) {
        // 0보다 작은 페이지를 요청한 경우
        if (pageable.getPageNumber() < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }
        Page<SharePost> sharePosts = shareRepository.findAllByIsActiveTrue(pageable);
        return sharePosts.map(SharePostResponse::new);
    }
}
