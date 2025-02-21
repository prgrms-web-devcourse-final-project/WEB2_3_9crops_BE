package io.crops.warmletter.domain.badword.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BadWordResponse {
    private Long id;
    private String word;
}
