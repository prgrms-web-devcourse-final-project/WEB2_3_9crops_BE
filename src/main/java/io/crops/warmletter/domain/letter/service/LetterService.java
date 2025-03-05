package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.EvaluateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.TemporarySaveLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterDraftResponse;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.exception.*;
import io.crops.warmletter.domain.letter.repository.LetterMatchingRepository;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.facade.MemberFacade;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import io.crops.warmletter.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.crops.warmletter.global.error.common.ErrorCode.INVALID_INPUT_VALUE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LetterService {

    private final LetterMatchingRepository letterMatchingRepository;
    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final BadWordService badWordService;
    private final MemberFacade memberFacade;
    private final AuthFacade authFacade;

    private final NotificationFacade notificationFacade;

    @Transactional
    public LetterResponse createLetter(CreateLetterRequest request) {
        badWordService.validateText(request.getTitle());
        badWordService.validateText(request.getContent());

        Long writerId = authFacade.getCurrentUserId(); //현재 로그인한 유저 id

        Letter.LetterBuilder builder = Letter.builder()
                .writerId(writerId)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .fontType(request.getFontType())
                .paperType(request.getPaperType());

        //랜덤 편지로 가는 첫 편지 작성, 받는사람, 상위편지가 없으면 첫 편지 전송
        if (request.getReceiverId() == null) {
            builder.receiverId(null)
                    .parentLetterId(null)
                    .letterType(LetterType.RANDOM)
                    .status(Status.DELIVERED)
                    .matchingId(null);
        }
        //주고받는 답장편지, 랜덤편지에 대한 답장
        else {
            //부모편지 조회
            Letter parentLetter = letterRepository.findById(request.getParentLetterId()).orElseThrow(ParentLetterNotFoundException::new);

//            Long matchingId = request.getMatchingId() != null ? request.getMatchingId() : parentLetter.getMatchingId(); 만약을 위해..

            //현재 계속 주고 받을 수 있는 상황이면 답장 가능
            boolean active = letterMatchingRepository.findById(request.getMatchingId()).orElseThrow(MatchingNotFoundException::new).isActive();

            if (active) {
                builder.receiverId(request.getReceiverId())
                        .parentLetterId(request.getParentLetterId())
                        .letterType(LetterType.DIRECT)
                        .status(Status.IN_DELIVERY)
                        .matchingId(request.getMatchingId());

                //첫편지면 matchingId 넣어줌 , 받는사람도 넣어줌.
                if(parentLetter.getParentLetterId() == null) {
                    parentLetter.updateMatchingId(request.getMatchingId());
                    parentLetter.updateReceiverId(writerId);
                    parentLetter.updateLetterType(LetterType.DIRECT);
                    parentLetter.updateIsRead(true);
                }
            }
        }
        Letter letter = builder.build();
        Letter savedLetter = letterRepository.save(letter);

        String zipCode = authFacade.getZipCode(); //현제 로그인한 유저 ZipCode

        if(request.getReceiverId() != null){
            notificationFacade.sendNotification(zipCode,request.getReceiverId(), AlarmType.LETTER,savedLetter.getId().toString());
        }

        return LetterResponse.fromEntity(savedLetter, zipCode);
    }

    public List<LetterResponse> getPreviousLetters(Long letterId) {
        Long myId = authFacade.getCurrentUserId();

        Letter letter = letterRepository.findById(letterId).orElseThrow(LetterNotFoundException::new);
        Long parentLetterId = letter.getParentLetterId(); //답장하는 편지의 부모 id

        Long matchingId = letter.getMatchingId();
        LetterMatching letterMatching = letterMatchingRepository.findById(matchingId).orElseThrow(MatchingNotFoundException::new);
        if (!letterMatching.getFirstMemberId().equals(myId) && !letterMatching.getSecondMemberId().equals(myId)) {
            throw new MatchingNotBelongException();
        }

        List<Letter> lettersByParentId = letterRepository.findLettersByParentLetterId(parentLetterId); //부모아이디로 편지 찾기

        List<LetterResponse> responses = new ArrayList<>();
        for (Letter findLetter : lettersByParentId) {
            String zipCode = memberRepository.findById(findLetter.getWriterId()).orElseThrow(MemberNotFoundException::new).getZipCode();
            LetterResponse response = LetterResponse.fromEntityForPreviousLetters(findLetter,zipCode);
            responses.add(response);
        }
        return responses;
    }

    @Transactional //더티채킹
    public void deleteLetter(Long letterId) {
        Letter letter = letterRepository.findById(letterId).orElseThrow(LetterNotFoundException::new);
        letter.inactive();
    }


    @Transactional
    public LetterResponse getLetterById(Long letterId) {
        Long myId = authFacade.getCurrentUserId();
        Letter letter = letterRepository.findById(letterId).orElseThrow(LetterNotFoundException::new);

        Long matchingId = letter.getMatchingId();
        LetterMatching letterMatching = letterMatchingRepository.findById(matchingId).orElseThrow(MatchingNotFoundException::new);

        if (!letterMatching.getFirstMemberId().equals(myId) && !letterMatching.getSecondMemberId().equals(myId)) {
            throw new MatchingNotBelongException();
        }

        String zipCode = memberRepository.findById(letter.getWriterId()).orElseThrow(MemberNotFoundException::new).getZipCode(); //편지를 쓴 사람의 zipCode

        letter.updateIsRead(true); //편지 조회 시 읽기
        letterRepository.save(letter);

        return LetterResponse.fromEntityForDetailView(letter, zipCode, letterMatching.isActive());
    }


    @Transactional
    public void evaluateLetter(Long letterId, EvaluateLetterRequest request) {
        Long receiverId = authFacade.getCurrentUserId();

        Letter letter = letterRepository.findByIdAndReceiverId(letterId, receiverId)
                                        .orElseThrow(LetterNotBelongException::new);
        letter.updateIsEvaluated(true); //편자 평가여부 true변환

        memberFacade.applyEvaluationTemperature(letter.getWriterId(), request.getEvaluation());

    }

    @Transactional
    public LetterResponse temporarySaveLetter(Long letterId, TemporarySaveLetterRequest request) {
        Long writerId = authFacade.getCurrentUserId();
        String writerZipCode = authFacade.getZipCode();

        if (letterId != null) {
            Letter letter = letterRepository.findByIdAndWriterId(letterId, writerId)
                    .orElseThrow(LetterNotBelongException::new);

            letter.updateTemporarySave(
                    request.getReceiverId(),
                    request.getParentLetterId(),
                    request.getCategory(),
                    request.getTitle(),
                    request.getContent()
            );

            return LetterResponse.fromEntity(letter, writerZipCode);
        }
        else {

            Letter letter = Letter.builder()
                    .writerId(1L)
                    .letterType(LetterType.RANDOM)
                    .category(request.getCategory())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .status(Status.SAVED)
                    .fontType(request.getFontType())
                    .paperType(request.getPaperType())
                    .build();
            letterRepository.save(letter);

            return LetterResponse.fromEntity(letter, writerZipCode);
        }
    }

    /**
     * 임시 저장 편지 삭제
     */
    @Transactional
    public Map<String, Long> deleteTemporarySaveLetter(Long letterId) {
        Long writerId = authFacade.getCurrentUserId();
        Letter letter = letterRepository.findByIdAndWriterIdAndStatusIsSAVED(letterId,writerId).orElseThrow(LetterNotFoundException::new);
        letterRepository.delete(letter);
        return Map.of("letterId",letter.getId());
    }

    /**
     * 오고 있는 편지 조회, 임시 저장된 편지 리스트 조회
     */
    public List<LetterResponse> getLettersByStatus(String status) {
        Long currentUserId = authFacade.getCurrentUserId();
        String formattedStatus = status.trim().toLowerCase();

        if ("delivery".equals(formattedStatus)) {
            // 받은 편지이면서 상태가 IN_DELIVERY인 편지 조회
            return letterRepository.findByReceiverIdAndStatus(currentUserId, Status.IN_DELIVERY)
                    .stream()
                    .map(LetterResponse::fromDeliveryLetter)
                    .collect(Collectors.toList());

        } else if ("draft".equals(formattedStatus)) {
            // 임시 저장 편지이면서 상태가 SAVED인 편지 조회 (작성자 기준)
            List<LetterDraftResponse> drafts = letterRepository.findDraftLettersWithMatching(currentUserId, Status.SAVED);
            return drafts.stream()
                    .map(draft -> LetterResponse.builder()
                            .letterId(draft.getLetterId())
                            .writerId(draft.getWriterId())
                            .receiverId(draft.getReceiverId())
                            .parentLetterId(draft.getParentLetterId())
                            .zipCode(authFacade.getZipCode())
                            .title(draft.getTitle())
                            .content(draft.getContent())
                            .category(draft.getCategory())
                            .paperType(draft.getPaperType())
                            .fontType(draft.getFontType())
                            .status(draft.getStatus())
                            .matched(draft.isMatched())
                            .deliveryStartedAt(draft.getDeliveryStartedAt())
                            .deliveryCompletedAt(draft.getDeliveryCompletedAt())
                            .matchingId(draft.getMatchingId())
                            .build())
                    .collect(Collectors.toList());
        } else {
            throw new BusinessException(INVALID_INPUT_VALUE);
        }
    }

    public int getLetterUnreadCount() {
        Long memberId = authFacade.getCurrentUserId();

        return letterRepository.countLetterUnreadCount(memberId);
    }
}
