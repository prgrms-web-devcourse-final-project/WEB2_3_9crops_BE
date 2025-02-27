package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.dto.response.MailboxDetailResponse;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.exception.MatchingAlreadyBlockedException;
import io.crops.warmletter.domain.letter.exception.MatchingNotBelongException;
import io.crops.warmletter.domain.letter.exception.MatchingNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterMatchingRepository;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailboxService {

    private final LetterMatchingRepository letterMatchingRepository;
    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final AuthFacade authFacade;


    public List<MailboxResponse> getMailbox(){
        Long myId = authFacade.getCurrentUserId();
        List<Long> matchedMembers = letterMatchingRepository.findMatchedMembers(myId); //매칭되고 나 말고 상대방 (예를들어 id 2,3) 중복은 제거
        log.info("Matched members: {}", matchedMembers);
        List<MailboxResponse> responses = new ArrayList<>();


        for (Long matchedMemberId : matchedMembers) {
            Member otherPerson= memberRepository.findById(matchedMemberId).orElseThrow(MemberNotFoundException::new);
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
                Boolean isRead = letter.isRead(); //편지가 읽어졌는지 todo 내 편지함 상세 조회 로직으로 다 찾아서 확인 해야 함. -> 전부 다 읽으면 true , 아니면 false로 반환

                MailboxResponse response = MailboxResponse.builder()
                        .letterMatchingId(letterMatchingId) //매칭id
                        .oppositeZipCode(zipCode) //상대방의 id
                        .isActive(isActive) // 방이 활성화인지
                        .isOppositeRead(isRead)
                        .build();
                responses.add(response);
            }
        }
        responses.sort(Comparator.comparing(MailboxResponse::getLetterMatchingId).reversed()); //아이디로 정렬 (시간순)
        return responses;

    }

    public Page<MailboxDetailResponse> detailMailbox(Long matchingId, Pageable pageable) {
        // 1. 현재 로그인된 사용자 ID 조회
        Long myId = authFacade.getCurrentUserId();

        // 2. matchingId로 LetterMatching 조회 (존재하지 않으면 MatchingNotFoundException 발생)
        LetterMatching matching = letterMatchingRepository.findById(matchingId)
                .orElseThrow(MatchingNotFoundException::new);

        // 3. 현재 사용자가 해당 매칭의 사용자 중 하나인지 검증
        if (!matching.getFirstMemberId().equals(myId) && !matching.getSecondMemberId().equals(myId)) {
            throw new MatchingNotBelongException();
        }

        // 4. LetterRepository에서 matchingId에 해당하는 편지들을 조회(정렬 및 페이징 적용)
        Page<Letter> letterPage = letterRepository.findByMatchingIdOrderByIdDesc(matchingId, pageable);

        // 5. 조회된 Letter 엔티티들을 MailboxDetailResponse DTO로 변환
        Page<MailboxDetailResponse> responses = letterPage.map(letter ->
                MailboxDetailResponse.builder()
                        .letterId(letter.getId())
                        .title(letter.getTitle())
                        .myLetter(letter.getWriterId().equals(myId))
                        .active(matching.isActive())
                        .createdAt(letter.getCreatedAt())
                        .build()
        );
        return responses;
    }

    @Transactional
    public void disconnectMatching(Long matchingId) {
        Long memberId = authFacade.getCurrentUserId();

        LetterMatching matching = letterMatchingRepository.findById(matchingId)
                .orElseThrow(MatchingNotFoundException::new);

        if (!letterMatchingRepository.existsByIdAndFirstMemberIdOrSecondMemberId(matchingId, memberId, memberId)) {
            throw new MatchingNotBelongException();
        }

        if (!matching.isActive()) {
            throw new MatchingAlreadyBlockedException();
        }

        matching.inactive();
    }
}
