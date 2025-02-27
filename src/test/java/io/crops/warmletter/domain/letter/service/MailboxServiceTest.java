package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.response.MailboxDetailResponse;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.crops.warmletter.domain.letter.exception.MatchingAlreadyBlockedException;
import io.crops.warmletter.domain.letter.exception.MatchingNotBelongException;
import io.crops.warmletter.domain.letter.exception.MatchingNotFoundException;
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
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailboxServiceTest {

    @Mock
    private LetterMatchingRepository letterMatchingRepository;

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private MailboxService mailBoxService;

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

    @DisplayName("매칭 차단 실패 - 존재하지 않은 매칭")
    @Test
    void disconnectMatching_WithNotFoundMatching_ShouldThrowException() throws Exception {
        //given
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        Long InvalidMatchingId = 999L;
        when(letterMatchingRepository.findById(InvalidMatchingId))
                .thenReturn(Optional.empty());

        //when & then
        assertThrows(MatchingNotFoundException.class,
                () -> mailBoxService.disconnectMatching(InvalidMatchingId));

        verify(authFacade).getCurrentUserId();
        verify(letterMatchingRepository).findById(InvalidMatchingId);
    }

    @DisplayName("매칭 차단 실패 - 속하지 않은 매칭")
    @Test
    void disconnectMatching_WithInvalidMatchingId_ShouldThrowException() throws Exception {
        //given
        Long firstMemberId = 1L;
        Long secondMemberId = 2L;
        when(authFacade.getCurrentUserId()).thenReturn(firstMemberId);

        Long matchingId = 1L;
        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(firstMemberId)
                .secondMemberId(secondMemberId)
                .build();
        when(letterMatchingRepository.findById(matchingId))
                .thenReturn(Optional.of(matching));
        when(letterMatchingRepository.existsByIdAndFirstMemberIdOrSecondMemberId(matchingId, firstMemberId, firstMemberId))
                .thenReturn(Boolean.FALSE);

        //when & then
        assertThrows(MatchingNotBelongException.class,
                () -> mailBoxService.disconnectMatching(matchingId));

        verify(authFacade).getCurrentUserId();
        verify(letterMatchingRepository).findById(matchingId);
        verify(letterMatchingRepository).existsByIdAndFirstMemberIdOrSecondMemberId(matchingId, firstMemberId, firstMemberId);
    }

    @DisplayName("매칭 차단 실패 - 이미 차단된 매칭")
    @Test
    void disconnectMatching_WithAlreadyDisconnectMatching_ShouldException() throws Exception {
        //given
        Long firstMemberId = 1L;
        Long secondMemberId = 2L;
        when(authFacade.getCurrentUserId()).thenReturn(firstMemberId);

        Long matchingId = 1L;
        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(firstMemberId)
                .secondMemberId(secondMemberId)
                .build();

        Field isActiveField = LetterMatching.class.getDeclaredField("isActive");
        isActiveField.setAccessible(true);
        isActiveField.set(matching, false);

        when(letterMatchingRepository.findById(matchingId))
                .thenReturn(Optional.of(matching));
        when(letterMatchingRepository.existsByIdAndFirstMemberIdOrSecondMemberId(matchingId, firstMemberId, firstMemberId))
                .thenReturn(Boolean.TRUE);


        //when & then
        assertThrows(MatchingAlreadyBlockedException.class,
                () -> mailBoxService.disconnectMatching(matchingId));

        verify(authFacade).getCurrentUserId();
        verify(letterMatchingRepository).findById(matchingId);
        verify(letterMatchingRepository).existsByIdAndFirstMemberIdOrSecondMemberId(matchingId, firstMemberId, firstMemberId);
    }

    @DisplayName("매칭 차단 성공")
    @Test
    void disconnectMatching_Success() throws Exception {
        //given
        Long firstMemberId = 1L;
        Long secondMemberId = 2L;
        when(authFacade.getCurrentUserId()).thenReturn(firstMemberId);

        Long matchingId = 1L;
        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(firstMemberId)
                .secondMemberId(secondMemberId)
                .build();

        when(letterMatchingRepository.findById(matchingId))
                .thenReturn(Optional.of(matching));
        when(letterMatchingRepository.existsByIdAndFirstMemberIdOrSecondMemberId(matchingId, firstMemberId, firstMemberId))
                .thenReturn(Boolean.TRUE);


        //when & then
        mailBoxService.disconnectMatching(matchingId);
        assertThat(matching.isActive()).isFalse();

        verify(authFacade).getCurrentUserId();
        verify(letterMatchingRepository).findById(matchingId);
        verify(letterMatchingRepository).existsByIdAndFirstMemberIdOrSecondMemberId(matchingId, firstMemberId, firstMemberId);
    }


    @DisplayName("편지함 상세 조회 성공 테스트")
    @Test
    void detailMailbox_Success() {
        // given
        Long myId = 1L;
        Long matchingId = 100L;
        Long oppositeId = 2L;

        // 현재 사용자 ID 설정
        when(authFacade.getCurrentUserId()).thenReturn(myId);

        // 매칭 객체 생성
        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(myId)
                .secondMemberId(oppositeId)
                .build();
        ReflectionTestUtils.setField(matching, "isActive", true);

        when(letterMatchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // 편지 객체 생성
        Letter myLetter = Letter.builder()
                .writerId(myId)
                .title("내가 보낸 편지")
                .content("내가 보낸 내용")
                .build();
        ReflectionTestUtils.setField(myLetter, "id", 10L);
        ReflectionTestUtils.setField(myLetter, "createdAt", LocalDateTime.now().minusDays(1));

        Letter oppositeLetter = Letter.builder()
                .writerId(oppositeId)
                .title("상대방이 보낸 편지")
                .content("상대방이 보낸 내용")
                .build();
        ReflectionTestUtils.setField(oppositeLetter, "id", 11L);
        ReflectionTestUtils.setField(oppositeLetter, "createdAt", LocalDateTime.now());

        List<Letter> letters = List.of(oppositeLetter, myLetter);

        // 페이징 처리
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<Letter> letterPage = new PageImpl<>(letters, pageable, letters.size());

        when(letterRepository.findByMatchingIdOrderByIdDesc(matchingId, pageable)).thenReturn(letterPage);

        // when
        Page<MailboxDetailResponse> result = mailBoxService.detailMailbox(matchingId, pageable);

        // then
        MailboxDetailResponse firstResponse = result.getContent().get(0);
        assertEquals(11L, firstResponse.getLetterId());
        assertEquals("상대방이 보낸 편지", firstResponse.getTitle());

        MailboxDetailResponse secondResponse = result.getContent().get(1);
        assertEquals(10L, secondResponse.getLetterId());
        assertEquals("내가 보낸 편지", secondResponse.getTitle());

        // 호출 검증
        verify(authFacade).getCurrentUserId();
        verify(letterMatchingRepository).findById(matchingId);
        verify(letterRepository).findByMatchingIdOrderByIdDesc(matchingId, pageable);
    }


    @DisplayName("편지함 상세 조회 실패 - 속하지 않은 매칭")
    @Test
    void detailMailbox_WithNotBelongMatching_ShouldThrowException() {
        // given
        Long myId = 1L;
        Long matchingId = 100L;
        Long firstMemberId = 2L;
        Long secondMemberId = 3L;

        when(authFacade.getCurrentUserId()).thenReturn(myId);

        // 현재 사용자가 속하지 않은 매칭 생성
        LetterMatching matching = LetterMatching.builder()
                .firstMemberId(firstMemberId)
                .secondMemberId(secondMemberId)
                .build();

        when(letterMatchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThrows(MatchingNotBelongException.class,
                () -> mailBoxService.detailMailbox(matchingId, pageable));

        verify(authFacade).getCurrentUserId();
        verify(letterMatchingRepository).findById(matchingId);
    }

}