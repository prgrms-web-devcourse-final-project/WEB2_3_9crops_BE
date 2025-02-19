package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CreateLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LetterService {

    private final LetterRepository letterRepository;

    @Transactional
    public CreateLetterResponse createLetter(CreateLetterRequest request) {

        Long writerId = 1L; // TODO: 실제 인증 정보를 사용하도록 변경
        Letter.LetterBuilder builder = Letter.builder()
                .writerId(writerId)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .fontType(request.getFont())
                .paperType(request.getPaperType());

        //랜덤 편지로 가는 첫 편지 작성, 받는사람, 상위편지가 없으면 첫 편지 전송
        if (request.getReceiverId() == null) {
            builder.receiverId(null)
                    .parentLetterId(null)
                    .letterType(LetterType.RANDOM);
        }
        //주고받는 답장편지, 랜덤편지에 대한 답장
        else {
            builder.receiverId(request.getReceiverId())
                    .parentLetterId(request.getParentLetterId())
                    .letterType(LetterType.DIRECT);
        }

        Letter letter = builder.build();
        Letter savedLetter = letterRepository.save(letter);
        return CreateLetterResponse.fromEntity(savedLetter);
    }

    public List<CreateLetterResponse> getPreviousLetters(Long letterId) {
        List<Letter> previousLetters = new ArrayList<>();

        // 현재 편지를 찾음
        Letter currentLetter = letterRepository.findById(letterId)
                .orElseThrow(() -> new IllegalArgumentException("해당 편지를 찾을 수 없습니다."));

        Long targetReceiverId = currentLetter.getReceiverId(); // 같은 작성자의 편지만 조회

        while (currentLetter.getParentLetterId() != null) {
            Letter parentLetter = letterRepository.findById(currentLetter.getParentLetterId())
                    .orElse(null);

            if (parentLetter == null || !parentLetter.getWriterId().equals(targetReceiverId)) {
                break; // 같은 작성자가 아니면 중단
            }

            previousLetters.add(parentLetter);
            currentLetter = parentLetter; // parentLetter를 계속 따라감
        }

        return previousLetters.stream()
                .map(CreateLetterResponse::fromEntity)
                .toList();
    }
}
