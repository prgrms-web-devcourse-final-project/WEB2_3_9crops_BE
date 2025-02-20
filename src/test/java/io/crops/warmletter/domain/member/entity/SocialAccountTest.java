package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.enums.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SocialAccountTest {

    @DisplayName("SocialAccount 연관관계 설정 테스트")
    @Test
    void setSocialAccount() {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .role(Role.USER)
                .build();

        SocialAccount socialAccount = SocialAccount.builder()
                .provider(SocialProvider.GOOGLE)
                .socialId("123")
                .build();

        socialAccount.setMember(member);

        // when & then
        assertThat(socialAccount.getMember()).isEqualTo(member);
    }
}