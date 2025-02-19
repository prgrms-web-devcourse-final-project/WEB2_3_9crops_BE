package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LetterService {

    private final LetterRepository lettersRepository;

    @Transactional
    public LetterResponse write(CreateLetterRequest request){

        Letter letter;

        //랜덤 편지로 가는 첫 편지 작성
        if(request.getReceiverId() == null){  //받는사람, 상위편지가 없으면 첫 편지 전송
            letter = Letter.builder()
                    .writerId(1L) //todo 내 아이디 넣어야 함
                    .receiverId(null) //받는사람
                    .parentLetterId(null) //상위편지 아이디
                    .letterType(LetterType.RANDOM)
                    .category(request.getCategory())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .fontType(request.getFont())
                    .paperType(request.getPaperType())
                    .build();
        }

        //주고받는 답장편지, 랜덤편지에 대한 답장
        else{
            letter = Letter.builder()
                    .writerId(1L) //todo 내 아이디 넣어야 함
                    .receiverId(request.getReceiverId())
                    .parentLetterId(request.getParentLetterId())
                    .letterType(LetterType.DIRECT)
                    .category(request.getCategory())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .fontType(request.getFont())
                    .paperType(request.getPaperType())
                    .build();
        }
        Letter saveLetter = lettersRepository.save(letter);
        return LetterResponse.fromEntity(saveLetter);
    }
}
