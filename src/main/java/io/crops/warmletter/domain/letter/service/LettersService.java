package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.entity.Letters;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.repository.LettersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LettersService {

    private final LettersRepository lettersRepository;


    public void write(CreateLetterRequest request){

        //랜덤 편지로 가는 첫 편지 작성
        if(request.getReceiverId() == null){ //받는사람
            Letters letters = Letters.builder()
                    .writerId(1L) //todo 내 아이디 넣어야 함
                    .receiverId(null)
                    .parentLetterId(null) //상위편지 아이디
                    .letterType(LetterType.RANDOM)
                    .category(request.getCategory())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .fontType(request.getFont())
                    .paperType(request.getPaperType())
                    .build();
            lettersRepository.save(letters);
        }

        //주고받는 답장편지
        else if (request.getCategory() == null) {
            Letters letters = Letters.builder()
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
            lettersRepository.save(letters);
        }
    }
}
