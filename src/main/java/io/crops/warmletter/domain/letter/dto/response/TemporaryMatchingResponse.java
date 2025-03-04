package io.crops.warmletter.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemporaryMatchingResponse {

    private final Long letterId;

    private final String title;      // 매칭된 편지의 제목

    private final String content;

    private final Long writerId;

    private final String zipCode;          // 현재 로그인한 회원의 우편번호

    private final Category category;       // 편지의 카테고리

    private final PaperType paperType;

    private final FontType fontType;

    private final LocalDateTime createdAt; // 편지 작성 시간

    private final LocalDateTime replyDeadLine; // 답장 제한 시간

    private final boolean isTemporary;     //임시테이블이 있는지 여부


    // 정적 메서드 추가: 매칭 데이터가 있을 때
    public static TemporaryMatchingResponse fromMatching(Letter letter, LetterTemporaryMatching tempMatching, String zipCode) {
        return TemporaryMatchingResponse.builder()
                .letterId(letter.getId())
                .title(letter.getTitle())
                .content(letter.getContent())
                .writerId(tempMatching.getFirstMemberId())
                .zipCode(zipCode)
                .category(letter.getCategory())
                .paperType(letter.getPaperType())
                .fontType(letter.getFontType())
                .createdAt(letter.getCreatedAt())
                .replyDeadLine(tempMatching.getReplyDeadLine())
                .isTemporary(true)
                .build();
    }

    // 정적 메서드 추가: 매칭 데이터가 없을 때
    public static TemporaryMatchingResponse empty() {
        return TemporaryMatchingResponse.builder()
                .isTemporary(false)
                .build();
    }
}
