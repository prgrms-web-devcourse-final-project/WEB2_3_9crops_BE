package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.EvaluateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.LetterEvaluation;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.exception.LetterNotBelongException;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.TemperaturePolicy;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LetterService {

    private final LetterRepository letterRepository;
    private final MemberRepository memberRepository;
    private final AuthFacade authFacade;
    private final BadWordService badWordService;

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
                .fontType(request.getFont())
                .paperType(request.getPaperType());

        //랜덤 편지로 가는 첫 편지 작성, 받는사람, 상위편지가 없으면 첫 편지 전송
        if (request.getReceiverId() == null) {
            builder.receiverId(null)
                    .parentLetterId(null)
                    .letterType(LetterType.RANDOM)
                    .status(Status.IN_DELIVERY);
        }
        //주고받는 답장편지, 랜덤편지에 대한 답장
        else {
            builder.receiverId(request.getReceiverId())
                    .parentLetterId(request.getParentLetterId())
                    .letterType(LetterType.DIRECT)
                    .status(Status.IN_DELIVERY);
        }
        Letter letter = builder.build();
        Letter savedLetter = letterRepository.save(letter);

        String zipCode = authFacade.getZipCode(); //현제 로그인한 유저 ZipCode

        return LetterResponse.fromEntity(savedLetter, zipCode);
    }

    public List<LetterResponse> getPreviousLetters(Long letterId) {

        Letter letter = letterRepository.findById(letterId).orElseThrow(LetterNotFoundException::new);
        Long parentLetterId = letter.getParentLetterId(); //답장하는 편지의 부모 id

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


    public LetterResponse getLetterById(Long letterId) {
        Letter letter = letterRepository.findById(letterId).orElseThrow(LetterNotFoundException::new);
        String zipCode = memberRepository.findById(letter.getWriterId()).orElseThrow(MemberNotFoundException::new).getZipCode(); //편지를 쓴 사람의 zipCode
        return LetterResponse.fromEntityForDetailView(letter, zipCode);
    }

    @Transactional
    public void evaluateLetter(Long letterId, EvaluateLetterRequest request) {
        Long receiverId = authFacade.getCurrentUserId();

        Letter letter = letterRepository.findByIdAndReceiverId(letterId, receiverId)
                                        .orElseThrow(LetterNotBelongException::new);

        Member evaluatedMember = memberRepository.findById(letter.getWriterId())
                                                    .orElseThrow(MemberNotFoundException::new);

        if (request.getEvaluation() == LetterEvaluation.GOOD) {
            evaluatedMember.applyTemperaturePolicy(TemperaturePolicy.GOOD_EVALUATION);
        } else if (request.getEvaluation() == LetterEvaluation.BAD) {
            evaluatedMember.applyTemperaturePolicy(TemperaturePolicy.BAD_EVALUATION);
        }

    }
}
