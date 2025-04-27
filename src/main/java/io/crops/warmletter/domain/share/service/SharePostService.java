package io.crops.warmletter.domain.share.service;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.dto.response.CursorResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.exception.ShareAccessException;
import io.crops.warmletter.domain.share.exception.SharePostNotFoundException;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharePostService {

    private final SharePostRepository sharePostRepository;
    private final AuthFacade authFacade;

    @Transactional(readOnly = true)
    public CursorResponse<SharePostResponse> getAllPosts(Long cursorId, int size) {
        List<SharePostResponse> responses = sharePostRepository.findAllActiveSharePostsWithZipCodes(cursorId, size+1);

        boolean hasNext = responses.size()>size;

        if (hasNext) {
            responses = responses.subList(0,size);
        }
        Long nextCursorId = hasNext && !responses.isEmpty() ? responses.get(responses.size() - 1).getSharePostId() : null;

        return new CursorResponse<>(responses, nextCursorId, hasNext);
    }

    @Transactional(readOnly = true)
    public SharePostDetailResponse getPostDetail(Long sharePostId) {

        return sharePostRepository.findDetailById(sharePostId)
                .orElseThrow(() -> new SharePostNotFoundException());
    }

    @Transactional(readOnly = true)
    public List<SharePostResponse> getMySharePosts() {
        Long memberId = authFacade.getCurrentUserId();

        return sharePostRepository.findMyRequestedActiveSharePosts(memberId);
    }

    @Transactional
    public void deleteSharePost(Long sharePostId) {
        Long memberId = authFacade.getCurrentUserId();

        SharePost sharePost = sharePostRepository.findByIdAndRequesterId(sharePostId, memberId)
                .orElseThrow(() -> new ShareAccessException());

        sharePost.deactivate();
    }
}
