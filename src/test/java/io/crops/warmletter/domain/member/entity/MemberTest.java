package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.member.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @DisplayName("Member 엔티티 생성")
    @Test
    void createMember() {
        //given
        String email = "test@test.com";

        Member member = Member.builder()
                .email(email)
                .password("password")
                .role(Role.USER)
                .build();
        
        //when & then
        assertThat(member.getEmail()).isEqualTo(email);
    }
}