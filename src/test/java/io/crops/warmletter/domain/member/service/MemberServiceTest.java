package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @DisplayName("회원 이메일 업데이트")
    @Test
    void updateMemberEmail() {
        // given
        String oldEmail = "old@example.com";
        String newEmail = "new@example.com";
        String socialUniqueId = "GOOGLE_12345";

        Member member = Member.builder()
                .email(oldEmail)
                .socialUniqueId(socialUniqueId)
                .role(Role.USER)
                .build();

        // when
        memberService.updateMemberEmail(member, newEmail);

        // then
        assertThat(member.getEmail()).isEqualTo(newEmail);
    }
}