package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void updateMemberEmail(Member member, String newEmail) {
        member.updateEmail(newEmail);
        memberRepository.save(member);

        // 관련된 리프레시 토큰 삭제
        redisTemplate.delete("refresh_token:" + member.getSocialUniqueId());  // socialUniqueId 기반으로 변경
    }
}
