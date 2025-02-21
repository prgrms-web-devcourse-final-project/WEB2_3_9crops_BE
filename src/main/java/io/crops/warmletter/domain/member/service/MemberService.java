package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void updateMemberEmail(Member member, String newEmail) {
        member.updateEmail(newEmail);
    }
}
