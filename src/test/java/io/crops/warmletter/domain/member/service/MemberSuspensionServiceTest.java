package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberSuspensionServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberSuspensionService memberSuspensionService;

    @DisplayName("정지 당할 인원이 3명 있을 경우 - 3명 정지")
    @Test
    void suspendMembersWithExcessiveWarnings() throws Exception {
        //given
        int expectedSuspendedCount = 3;
        when(memberRepository.suspendMembersWithExcessiveWarnings()).thenReturn(expectedSuspendedCount);

        //when
        int actualSuspendedCount = memberSuspensionService.suspendMembersWithExcessiveWarnings();

        //then
        assertThat(expectedSuspendedCount).isEqualTo(actualSuspendedCount);
        verify(memberRepository).suspendMembersWithExcessiveWarnings();
    }
}