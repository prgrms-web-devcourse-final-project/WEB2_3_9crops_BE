package io.crops.warmletter.domain.member.facade;

import io.crops.warmletter.domain.letter.enums.LetterEvaluation;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.TemperaturePolicy;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFacade {

    private final MemberService memberService;

    public void updateMemberEmail(Member member, String newEmail) {
        memberService.updateMemberEmail(member, newEmail);
    }

    public void applyEvaluationTemperature(Long memberId, LetterEvaluation evaluation) {
        memberService.applyEvaluationTemperature(memberId, evaluation);
    }
}
