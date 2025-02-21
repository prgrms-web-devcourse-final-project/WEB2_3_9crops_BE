package io.crops.warmletter.domain.badword.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BadWordsResponse {
    private List<String> words;

}
