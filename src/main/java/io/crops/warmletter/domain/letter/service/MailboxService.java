package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.response.MailboxDetailResponse;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.exception.MatchingAlreadyBlockedException;
import io.crops.warmletter.domain.letter.exception.MatchingNotBelongException;
import io.crops.warmletter.domain.letter.exception.MatchingNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterMatchingRepository;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<MailboxResponse> responses = letterMatchingRepository.findMailboxDetails(myId);
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
