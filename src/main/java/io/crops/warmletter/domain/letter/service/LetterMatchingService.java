package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LetterMatchingService {

    private final LetterRepository letterRepository;

    public List<RandomLetterResponse> findRandomLetters(Category category) {
        Pageable pageable = PageRequest.of(0, 5);  // 첫 페이지, 5개 제한
        return letterRepository.findRandomLettersByCategory(category, pageable);
    }
}
