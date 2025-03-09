package io.crops.warmletter.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LetterResponse {

    //편지 아이디
    private final Long letterId;

    //보낸 사람 아이디
    private final Long writerId;

    //받는사람 아이디 -> null이면 첫편지
    private final Long receiverId;

    //상위 편지 id -> null이면 첫편지
    private final Long parentLetterId; //상위 편지 id

    private final String zipCode;

    private final String title;

    private final String content;

    private final Category category;

    private final PaperType paperType;

    private final FontType fontType;

    private final Status status;

    private final LocalDateTime deliveryStartedAt;

    private final LocalDateTime deliveryCompletedAt;

    private final Boolean matched;

    private final Long matchingId; //편지 조회

    private final Boolean evaluated; //편지 평가 여부

    private final Long memberId;


    //공통 변환
    public static LetterResponse fromEntity(Letter letter, String zipCode) {
        return LetterResponse.builder()
                .letterId(letter.getId())
                .writerId(letter.getWriterId())
                .receiverId(letter.getReceiverId())
                .parentLetterId(letter.getParentLetterId())
                .zipCode(zipCode)
                .title(letter.getTitle())
                .content(letter.getContent())
                .category(letter.getCategory())
                .paperType(letter.getPaperType())
                .fontType(letter.getFontType())
                .status(letter.getStatus())
                .deliveryStartedAt(letter.getDeliveryStartedAt())
                .deliveryCompletedAt(letter.getDeliveryCompletedAt())
                .matchingId(letter.getMatchingId())
                .build();
    }

    //이전편지 변환
    public static LetterResponse fromEntityForPreviousLetters(Letter letter, String zipCode, Long matchingId) {
        return LetterResponse.builder()
                .letterId(letter.getId()) //이전 편지의 정보들
                .zipCode(zipCode)
                .title(letter.getTitle())
                .content(letter.getContent())
                .category(letter.getCategory())
                .memberId(letter.getWriterId()) //이전 편지를 쓴 사용자
                .matchingId(matchingId)
                .build();
    }

    //단건조회에 사용
    public static LetterResponse fromEntityForDetailView(Letter letter, String zipCode, boolean matched) {
        return LetterResponse.builder()
                .letterId(letter.getId())
                .zipCode(zipCode)
                .title(letter.getTitle())
                .content(letter.getContent())
                .category(letter.getCategory())
                .paperType(letter.getPaperType())
                .fontType(letter.getFontType())
                .matched(matched)
                .matchingId(letter.getMatchingId())
                .evaluated(letter.isEvaluated())
                .build();
    }

    //임시 저장된 편지 리스트 조회
    public static LetterResponse fromDeliveryLetter(Letter letter) {
        return LetterResponse.builder()
                .letterId(letter.getId())
                .title(letter.getTitle())
                .deliveryStartedAt(letter.getDeliveryStartedAt())
                .deliveryCompletedAt(letter.getDeliveryCompletedAt())
                .build();
    }

    // 임시 저장(draft) 변환 메서드 추가
    public static LetterResponse fromDraftLetter(LetterDraftResponse draft) {
        return builder()
                .letterId(draft.getLetterId())
                .matchingId(draft.getMatchingId())
                .receiverId(draft.getReceiverId())
                .parentLetterId(draft.getParentLetterId())
                .title(draft.getTitle())
                .content(draft.getContent())
                .category(draft.getCategory())
                .paperType(draft.getPaperType())
                .fontType(draft.getFontType())
                .build();
    }
}
