package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LetterMatchingService {

    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;


    public List<RandomLetterResponse> findRandomLetters(String category) {
        List<Letter> letters;

        if(category == null || category.isEmpty()){ //전체 조회 시
            letters = letterRepository.findRandomLetters(5);
        }else { //카테고리로 조회 시
            letters = letterRepository.findRandomLettersByCategory(category, 5);
        }

        List<RandomLetterResponse> responses = new ArrayList<>();
        for (Letter letter : letters) {
            String zipCode = memberRepository.findById(letter.getWriterId())
                    .orElseThrow(MemberNotFoundException::new)
                    .getZipCode();

            RandomLetterResponse response = RandomLetterResponse.builder() //카테고리 필요할듯?
                    .letterId(letter.getId())
                    .content(letter.getContent())
                    .zipCode(zipCode)
                    .category(letter.getCategory())
                    .paperType(letter.getPaperType())
                    .fontType(letter.getFontType())
                    .createdAt(letter.getCreatedAt())
                    .build();
            responses.add(response);
        }
        return responses;
    }
}
