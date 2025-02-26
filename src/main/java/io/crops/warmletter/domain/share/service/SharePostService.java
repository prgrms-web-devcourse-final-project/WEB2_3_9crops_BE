package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.exception.ShareException;
import io.crops.warmletter.domain.share.exception.SharePostNotFoundException;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharePostService {

    private final SharePostRepository sharePostRepository;

    @Transactional(readOnly = true)
    public Page<SharePostResponse> getAllPosts(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new ShareException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        return sharePostRepository.findAllActiveSharePostsWithZipCodes(pageable);

    }

    @Transactional(readOnly = true)
    public SharePostDetailResponse getPostDetail(Long sharePostId) {
        return sharePostRepository.findDetailById(sharePostId)
                .orElseThrow(() -> new SharePostNotFoundException());
    }
}
