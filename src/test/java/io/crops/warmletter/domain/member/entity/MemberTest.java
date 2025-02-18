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
        float temperature = 36.5f;

        Member member = Member.builder()
                .email(email)
                .password("password")
                .temperature(temperature)
                .role(Role.USER)
                .build();
        
        //when & then
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getTemperature()).isEqualTo(temperature);
    }
}