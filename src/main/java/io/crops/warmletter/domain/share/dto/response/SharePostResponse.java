package io.crops.warmletter.domain.share.dto.response;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SharePostResponse {
    private Long sharePostId;
    private Long shareProposalId;
    private String writerZipCode;
    private String receiverZipCode;
    private String content;
    private boolean isActive;
    private LocalDateTime createdAt;

    public SharePostResponse(Long sharePostId, Long shareProposalId, String writerZipCode, String receiverZipCode,
                             String content, boolean isActive, LocalDateTime createdAt) {
        this.sharePostId = sharePostId;
        this.shareProposalId = shareProposalId;
        this.writerZipCode = writerZipCode;
        this.receiverZipCode = receiverZipCode;
        this.content = content;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
}
