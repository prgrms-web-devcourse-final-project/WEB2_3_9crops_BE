package io.crops.warmletter.domain.share.dto.response;
import io.crops.warmletter.domain.share.entity.SharePost;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class SharePostResponse {

    private Long sharePostId;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public SharePostResponse(SharePost sharePost) {
        this.sharePostId = sharePost.getId();
        this.title = sharePost.getTitle();
        this.content = sharePost.getContent();
        this.createdAt = sharePost.getCreatedAt();
    }

    public SharePost toEntity() {
        return SharePost.builder()
                .shareProposalId(this.sharePostId)
                .title(this.title)
                .content(this.content)
                .build();
    }
}
