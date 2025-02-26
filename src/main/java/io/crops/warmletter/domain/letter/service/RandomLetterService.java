package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.request.ApproveLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.exception.AlreadyApprovedException;
import io.crops.warmletter.domain.letter.exception.DuplicateLetterMatchException;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.exception.TemporaryMatchingNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.repository.LetterTemporaryMatchingRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RandomLetterService {

    private final LetterTemporaryMatchingRepository letterTemporaryMatchingRepository;
    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final AuthFacade authFacade;

    /**
     *  랜덤 편지 리스트 찾기 5개씩 (수정필요)
     */
    public List<RandomLetterResponse> findRandomLetters(Category category) {
        Long currentUserId = authFacade.getCurrentUserId();
        Pageable pageable = PageRequest.of(0, 5);  // 첫 페이지, 5개 제한

        if (category != null) { //전체 조회가 아닌 경우에 회원의 선호 카테고리를 변경
            Member member = memberRepository.findById(currentUserId).orElseThrow();
            member.updatePreferredLetterCategory(category);
        }

        return letterRepository.findRandomLettersByCategory(category, currentUserId, pageable);
    }

    /**
     * 현재 유저가 lastMatchedAt 시간부터 ~ +1시간 이내에 랜덤 편지를 보내는지 체크
     */
    public CheckLastMatchResponse checkLastMatched() {
        Long currentUserId = authFacade.getCurrentUserId();
        Member member = memberRepository.findById(currentUserId).orElseThrow();

        LocalDateTime lastMatchedAt = member.getLastMatchedAt();

        // 한 번도 편지를 보내지 않았거나, 마지막 전송 시간 + 1시간 이후이면 전송 가능
        if (lastMatchedAt == null || !LocalDateTime.now().isBefore(lastMatchedAt.plusHours(1))) {
            return CheckLastMatchResponse.builder()
                    .canSend(true)
                    .build();
        }
        // 그렇지 않으면 전송 불가능
        return CheckLastMatchResponse.builder()
                .canSend(false)
                .lastMatchedAt(lastMatchedAt)
                .build();
    }

    /**
     * 임시테이블에 회원이 있는지 검증
     */
    public TemporaryMatchingResponse checkTemporaryMatchedTable() {
        Long currentUserId = authFacade.getCurrentUserId();
        Optional<LetterTemporaryMatching> tempTable = letterTemporaryMatchingRepository.findBySecondMemberId(currentUserId);
        if (tempTable.isPresent()) {
            // 임시 매칭 데이터가 있으면 해당 편지 정보를 조회해서 응답 DTO에 채워줌~~
            LetterTemporaryMatching tempMatching = tempTable.get();

            Letter letter = letterRepository.findById(tempMatching.getLetterId())
                    .orElseThrow(LetterNotFoundException::new);

//            Member member = memberRepository.findById(6L).orElseThrow(); //테스트시 필요

            return TemporaryMatchingResponse.builder()
                    .letterId(letter.getId())
                    .content(letter.getContent())
                    .zipCode(authFacade.getZipCode())
                    .letterTitle(letter.getTitle())
                    .category(letter.getCategory())
                    .paperType(letter.getPaperType())
                    .fontType(letter.getFontType())
                    .createdAt(letter.getCreatedAt())
                    .replyDeadLine(tempMatching.getReplyDeadLine())
                    .isTemporary(true)
                    .build();
        } else {
            return TemporaryMatchingResponse.builder()
                    .isTemporary(false)
                    .build();
        }
    }

    /**
     * 랜덤편지 매칭 취소
     */
    @Transactional
    public void matchingCancel(){
        Long currentUserId = authFacade.getCurrentUserId();
        Optional<LetterTemporaryMatching> tempTable = letterTemporaryMatchingRepository.findBySecondMemberId(currentUserId);

        if (tempTable.isPresent()) {
            Letter letter = letterRepository.findById(tempTable.get().getLetterId()).orElseThrow(LetterNotFoundException::new);
            letter.updateLetterType(LetterType.RANDOM);
            letterTemporaryMatchingRepository.delete(tempTable.get());
        } else {
            throw new TemporaryMatchingNotFoundException();
        }
    }

    /**
     * 랜덤 편지 승인하기.
     */
    @Transactional
    public void approveLetter(ApproveLetterRequest request) {
        Long currentUserId = authFacade.getCurrentUserId();

        // 현재 사용자가 이미 다른 편지를 승인했는지 확인
        if (letterTemporaryMatchingRepository.findBySecondMemberId(currentUserId).isPresent()) {
            throw new AlreadyApprovedException();
        }

        Member member = memberRepository.findById(currentUserId).orElseThrow();

        LetterTemporaryMatching letterTemporaryMatching = LetterTemporaryMatching.builder()
                .letterId(request.getLetterId())
                .firstMemberId(request.getWriterId())
                .secondMemberId(currentUserId)
                .build();

        try {
            letterTemporaryMatchingRepository.save(letterTemporaryMatching);
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약조건 위반 시 중복 매칭이 발생한 것으로 판단하고 예외 던짐
            throw new DuplicateLetterMatchException();
        }

        Letter letter = letterRepository.findById(letterTemporaryMatching.getLetterId()).orElseThrow(LetterNotFoundException::new);
        letter.updateLetterType(LetterType.DIRECT);
        member.updateLastMatchedAt(letterTemporaryMatching.getMatchedAt());
    }



    //lastMatchedAt 매칭시 시간 넣어주기.
    //
//    /**
//     *  랜덤편지 매칭 로직
//     */
//    @Transactional
//    public void letterMatching(RandomMatchingRequest request){
//        Letter letter = letterRepository.findById(request.getLetterId()).orElseThrow(LetterNotFoundException::new);
//        Long currentUserId = authFacade.getCurrentUserId();
//
//        letter.setReceiverId(currentUserId);
//
//        //그리고
//        LetterMatching letterMatching = LetterMatching.builder()
//                .letterId(letter.getId())
//                .firstMemberId(letter.getWriterId())
//                .secondMemberId(currentUserId)
//                .build();
//
//        letterRepository.save(letter);
//        letterMatchingRepository.save(letterMatching);
//    }
}
