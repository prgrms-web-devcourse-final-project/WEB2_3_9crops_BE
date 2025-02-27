package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.member.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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


    @Test
    @DisplayName("경고 증가 - 3회 미만일 경우 비활성화 안됨")
    void increaseWarningCount_UnderLimit() throws Exception {
        // Given
        Member member = new Member(); // 기본 생성자로 생성

        // ⚠️ 리플렉션을 사용해 warningCount 필드 강제 변경
        Field warningCountField = Member.class.getDeclaredField("warningCount");
        warningCountField.setAccessible(true);
        warningCountField.set(member, 2); // 경고 2회로 설정

        // When
        member.increaseWarningCount();

        // Then
        assertThat(warningCountField.get(member)).isEqualTo(3);
        assertThat(member.isActive()).isFalse(); // 3회 이상이면 비활성화
    }

    @Test
    @DisplayName("경고 증가 - 이미 3회 이상이면 비활성화 상태 유지")
    void increaseWarningCount_AlreadyDeactivated() throws Exception {
        // Given
        Member member = new Member();

        // ⚠️ 리플렉션을 사용해 warningCount 필드 강제 변경
        Field warningCountField = Member.class.getDeclaredField("warningCount");
        warningCountField.setAccessible(true);
        warningCountField.set(member, 3); // 이미 3회로 설정

        // When
        member.increaseWarningCount();

        // Then
        assertThat(warningCountField.get(member)).isEqualTo(4); // 경고 증가
        assertThat(member.isActive()).isFalse(); // 여전히 비활성화
    }

}