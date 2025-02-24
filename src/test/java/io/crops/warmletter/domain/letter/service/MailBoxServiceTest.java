package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.repository.LetterMatchingRepository;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailBoxServiceTest {

    @Mock
    private LetterMatchingRepository letterMatchingRepository;

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private MailBoxService mailBoxService;

    @Test
    @DisplayName("내 편지함 목록 조회 성공 테스트")
    void getMailbox_success() {
        Long myId = 3L; //내 아이디
        when(authFacade.getCurrentUserId()).thenReturn(3L);


        // 1. myId(3L)와 매칭된 회원 목록: 회원 1이 매칭됨.
        List<Long> matchedMembers = List.of(1L);
        when(letterMatchingRepository.findMatchedMembers(myId)).thenReturn(matchedMembers); //나와 대화 나눈 상대방 아이디 1L

        // 2. 회원 1의 정보: 우편번호 "12345"를 가진 Member 객체 반환.
        Member matchedMember = Member.builder()
                .socialUniqueId("unique123")
                .email("user@example.com")
                .zipCode("12345")
                .password("hashedPassword")
                .temperature(36.5f)
                .preferredLetterCategory(null)
                .role(null)
                .lastMatchedAt(null)
                .build();
        ReflectionTestUtils.setField(matchedMember, "id", 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(matchedMember));

        LetterMatching letterMatching = LetterMatching.builder()
                .letterId(2L)
                .build();

        // letterMatching의 ID, letterId, isActive 설정 (setter가 없으면 Reflection 사용)
        ReflectionTestUtils.setField(letterMatching, "id", 100L);
        List<LetterMatching> matchingList = List.of(letterMatching);
        when(letterMatchingRepository.findMatchingIdsByMembers(myId, 1L)).thenReturn(matchingList);

        // 4. 매칭된 편지 정보: letterId 2인 편지, isRead = false.
        Letter matchedLetter = Letter.builder()
                .writerId(1L)
                .title("편지 제목")
                .content("편지 내용")
                .fontType(null)
                .paperType(null)
                .category(null)
                .receiverId(null)
                .parentLetterId(null)
                .build();
        ReflectionTestUtils.setField(matchedLetter, "id", 2L);
        ReflectionTestUtils.setField(matchedLetter, "isRead", false);
        when(letterRepository.findById(2L)).thenReturn(Optional.of(matchedLetter));

        // Act: 내 편지함 목록 조회
        List<MailboxResponse> responses = mailBoxService.getMailbox();

        // Assert: 결과 검증
        assertNotNull(responses);
        assertEquals(1, responses.size());
        MailboxResponse response = responses.get(0);
        assertEquals(100L, response.getLetterMatchingId());
        assertEquals("12345", response.getOppositeZipCode());
        assertTrue(response.isActive());
        assertFalse(response.isOppositeRead());

        // verify 호출
        verify(letterMatchingRepository).findMatchedMembers(myId);
        verify(memberRepository).findById(1L);
        verify(letterMatchingRepository).findMatchingIdsByMembers(myId, 1L);
        verify(letterRepository).findById(2L);
    }
}