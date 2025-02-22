package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterMatchingRepository;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailBoxService {

    private final LetterMatchingRepository letterMatchingRepository;
    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;


    public List<MailboxResponse> getMailbox(){
        Long myId = 3L; //todo 내 아이디 나중에 시큐리티 메서드에서 뽑아옴
        List<Long> matchedMembers = letterMatchingRepository.findMatchedMembers(myId); //매칭되고 나 말고 상대방 id 2,3 중복은 제거
        log.info("Matched members: {}", matchedMembers);
        List<MailboxResponse> responses = new ArrayList<>();


        for (Long matchedMemberId : matchedMembers) {
            Member otherPerson= memberRepository.findById(matchedMemberId).orElseThrow(); //todo 병합시 member Exception 추가
            Long id = otherPerson.getId(); //상대방 A을 찾고
            log.info("Matched member id: {}", id);
            String zipCode = otherPerson.getZipCode(); //상대방 A의 zipcode

            //상대방 a와 나와 나눴던 매칭 찾기 (여러번 매칭이 가능하니 여러 개 나옴)
            List<LetterMatching> lettersMatching = letterMatchingRepository.findMatchingIdsByMembers(myId, id);//나와, 상대방과 찾았던 편지 매칭 아이디를 찾음

            for (LetterMatching letterMatching : lettersMatching) { //상대방 A와 했던 매칭들 중 1
                Long letterMatchingId = letterMatching.getId();

                //매칭된 최초의 편지 id
                Letter letter = letterRepository.findById(letterMatching.getLetterId()).orElseThrow(LetterNotFoundException::new);
                Boolean isActive = letterMatching.isActive(); //매칭이 계속적으로 주고 받을 수 있는지
                Boolean isRead = letter.getIsRead(); //편지가 읽어졌는지 todo 내 편지함 상세 조회 로직으로 다 찾아서 확인 해야 함. -> 전부 다 읽으면 true , 아니면 false로 반환

                MailboxResponse response = MailboxResponse.builder()
                        .letterMatchingId(letterMatchingId) //매칭id
                        .oppositeZipCode(zipCode) //상대방의 id
                        .isActive(isActive) // 방이 활성화인지
                        .isOppositeRead(isRead)
                        .build();
                responses.add(response);
            }
        }
        responses.sort(Comparator.comparing(MailboxResponse::getLetterMatchingId));
        return responses;

    }
}
