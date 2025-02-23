package io.crops.warmletter.domain.share.dto.response;

import lombok.*;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ShareLetterDetailResponse {

    // 공유 제안 편지 클릭시
    // 상세 편지 정보가 나와야하는 클래스 .
    private Long sharePostId;
    private String zipCode;
    private String sharePostContent;
    private List<ShareLetterPostResponse> letters;

}

