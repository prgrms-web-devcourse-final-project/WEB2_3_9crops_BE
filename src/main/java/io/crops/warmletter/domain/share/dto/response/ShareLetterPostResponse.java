package io.crops.warmletter.domain.share.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLetterPostResponse {
    private Long id;
    private String content;
    private String writerZipCode;
    private String receiverZipCode;
    private LocalDateTime createdAt;

}