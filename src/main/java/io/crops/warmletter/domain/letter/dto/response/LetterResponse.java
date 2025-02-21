package io.crops.warmletter.domain.letter.dto.response;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.DeliveryStatus;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
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

    private final FontType fontType;

    private final DeliveryStatus deliveryStatus;

    private final LocalDateTime deliveryStartedAt;

    private final LocalDateTime deliveryCompletedAt;


    //공통 변환
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
                .fontType(letter.getFontType())
                .deliveryStatus(letter.getDeliveryStatus())
                .deliveryStartedAt(letter.getDeliveryStartedAt())
                .deliveryCompletedAt(letter.getDeliveryCompletedAt())
                .build();
    }

    //이전편지 변환
    public static LetterResponse fromEntityForPreviousLetters(Letter letter) {
        return LetterResponse.builder()
                .letterId(letter.getId())
                .title(letter.getTitle())
                .content(letter.getContent())
                .paperType(letter.getPaperType())
                .fontType(letter.getFontType())
                .build();
    }

    //단건조회에 사용
    public static LetterResponse fromEntityForDetailView(Letter letter) {
        return LetterResponse.builder()
                .letterId(letter.getId())
                .title(letter.getTitle())
                .content(letter.getContent())
                .paperType(letter.getPaperType())
                .fontType(letter.getFontType())
                .build();
    }
}
