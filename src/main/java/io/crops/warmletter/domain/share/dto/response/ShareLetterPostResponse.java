package io.crops.warmletter.domain.share.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareLetterPostResponse {
    private Long id;
    private String content;
    private String writerZipCode;
    private String receiverZipCode;
    private LocalDateTime createdAt;

    @Builder
    public ShareLetterPostResponse(Long id, String content, String writerZipCode, String receiverZipCode, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.writerZipCode = writerZipCode;
        this.receiverZipCode = receiverZipCode;
        this.createdAt = createdAt;
    }
}