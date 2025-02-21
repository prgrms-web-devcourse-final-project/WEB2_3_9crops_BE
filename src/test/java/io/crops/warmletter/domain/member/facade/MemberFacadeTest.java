package io.crops.warmletter.domain.member.facade;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberFacadeTest {

    @InjectMocks
    private MemberFacade memberFacade;

    @Mock
    private MemberService memberService;

    @DisplayName("회원 이메일 업데이트 요청이 서비스 계층으로 잘 전달되는지 확인")
    @Test
    void updateMemberEmail() {
        // given
        Member member = Member.builder()
                .email("old@example.com")
                .socialUniqueId("GOOGLE_12345")
                .role(Role.USER)
                .build();
        String newEmail = "new@example.com";

        // when
        memberFacade.updateMemberEmail(member, newEmail);

        // then
        verify(memberService).updateMemberEmail(member, newEmail);
    }
}