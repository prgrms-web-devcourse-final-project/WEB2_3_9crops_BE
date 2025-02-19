package io.crops.warmletter.domain.letter.dto.response;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.DeliveryStatus;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * {
 *   "data": {
 *     "letterId": 12345,
 *     "title": "저 요즘 슬퍼요..",
 *     "content": "블라블라",
 *     "category": "위로와 공감",
 *     "paperType": "편지지1번",
 *     "font": "3번글꼴",
 *     "deliveryStatus": "IN_DELIVERY",
 *     "deliveryStartedAt": "2025-02-17T15:30:00Z", //필요할까?
 *     "isRead": false, //필요할까?
 *     "isActive": true
 *   },
 *   "message": "편지가 성공적으로 생성되었습니다.",
 *   "timestamp": "2025-02-17T15:29:50Z"
 * }
 */
@Getter
public class LetterResponse {

    //편지 아이디
    private final Long letterId;

    //보낸 사람 아이디
    private final Long writerId;

    //받는사람 아이디 -> null이면 첫편지
    private final Long receiverId;

    //상위 편지 id -> null이면 첫편지
    private final Long parentLetterId; //상위 편지 id

    private final String title;

    private final String content;

    private final Category category; //null이면 주고받는 답장 편지

    private final PaperType paperType;

    private final FontType font;

    private final DeliveryStatus deliveryStatus;

    private final LocalDateTime deliveryStartedAt;

    private final LocalDateTime deliveryCompletedAt;


    @Builder
    public LetterResponse(Long letterId, Long writerId, Long receiverId, Long parentLetterId, String title, String content, Category category, PaperType paperType, FontType font, DeliveryStatus deliveryStatus, LocalDateTime deliveryStartedAt, LocalDateTime deliveryCompletedAt) {
        this.letterId = letterId;
        this.writerId = writerId;
        this.receiverId = receiverId;
        this.parentLetterId = parentLetterId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.paperType = paperType;
        this.font = font;
        this.deliveryStatus = deliveryStatus;
        this.deliveryStartedAt = deliveryStartedAt;
        this.deliveryCompletedAt = deliveryCompletedAt;
    }


    public static LetterResponse fromEntity(Letter letter) {
        return LetterResponse.builder()
                .letterId(letter.getId())
                .writerId(letter.getWriterId())
                .receiverId(letter.getReceiverId())
                .parentLetterId(letter.getParentLetterId())
                .title(letter.getTitle())
                .content(letter.getContent())
                .category(letter.getCategory())
                .paperType(letter.getPaperType())
                .font(letter.getFontType())
                .deliveryStatus(letter.getDeliveryStatus())
                .deliveryStartedAt(letter.getDeliveryStartedAt())
                .deliveryCompletedAt(letter.getDeliveryCompletedAt())
                .build();
    }
}
