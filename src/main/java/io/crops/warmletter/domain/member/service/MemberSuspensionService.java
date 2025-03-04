package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSuspensionService {

    private final MemberRepository memberRepository;

    @Transactional
    public int suspendMembersWithExcessiveWarnings() {
        return memberRepository.suspendMembersWithExcessiveWarnings();
    }
}
