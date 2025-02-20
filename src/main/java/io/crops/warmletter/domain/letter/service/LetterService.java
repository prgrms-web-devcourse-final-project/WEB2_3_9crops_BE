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
//        Long writerId = 1L; // TODO: 실제 인증 정보를 사용하도록 변경
        Letter.LetterBuilder builder = Letter.builder()
                .writerId(request.getWriterId())
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

        Letter letter = letterRepository.findById(letterId).orElseThrow(() -> new RuntimeException("해당 편지를 찾을 수 없습니다.")); //todo 에러처리
        Long parentLetterId = letter.getParentLetterId(); //답장하는 편지의 부모 id

        List<Letter> lettersByParentId = letterRepository.findLettersByParentLetterId(parentLetterId); //부모아이디로 편지 찾기

        List<CreateLetterResponse> responses = new ArrayList<>();
        for (Letter findLetter : lettersByParentId) {
            CreateLetterResponse response = CreateLetterResponse.fromEntity(findLetter);
            responses.add(response);
        }
        return responses;
    }
}
