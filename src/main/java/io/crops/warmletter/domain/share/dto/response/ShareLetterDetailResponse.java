package io.crops.warmletter.domain.share.dto.response;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

// 공유 제안 편지 클릭시 상세 정보가 나오도록 예를들어 공유제안편지 -> 상세 정보편지여러개나오도록
public class ShareLetterDetailResponse {

    // 공유 제안 편지 클릭시
    // 상세 편지 정보가 나와야하는 클래스 .
    private Long sharePostId;
    private String zipCode;
    private String sharePostContent;
    private List<ShareLetterPostResponse> letters;

    @Builder
    public ShareLetterDetailResponse(Long id, String zipCode, String sharePostContent, List<ShareLetterPostResponse> letters) {
        this.sharePostId = id;
        this.zipCode = zipCode;
        this.sharePostContent = sharePostContent;
        this.letters = letters != null ? letters : new ArrayList<>();
    }
}

