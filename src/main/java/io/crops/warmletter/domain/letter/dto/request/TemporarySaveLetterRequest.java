package io.crops.warmletter.domain.letter.dto.request;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TemporarySaveLetterRequest {

    private Long letterId;

    @NotNull(message = "받는 사람을 설정해주세요.")
    private Long receiverId;

    @NotNull(message = "매칭 번호를 설정해주세요.")
    private Long matchingId;

    @NotNull(message = "상위 편지를 설정해주세요.")
    private Long parentLetterId;

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private Category category;

    private PaperType paperType;

    private FontType fontType;
}
