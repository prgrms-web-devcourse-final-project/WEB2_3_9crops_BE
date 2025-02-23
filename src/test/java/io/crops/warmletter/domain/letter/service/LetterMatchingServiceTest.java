package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
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
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LetterMatchingServiceTest {

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LetterMatchingService letterMatchingService;


    @Test
    @DisplayName("랜덤 편지 리스트 확인 - 카테고리가 있을 경우")
    void find_RandomLetters_WithCategory() {
        String category = "CONSOLATION";

        // 10번 회원이 있고
        Member member = Member.builder()
                .zipCode("12345")
                .build();
        ReflectionTestUtils.setField(member, "id", 10L);

        // 10번 회원이 쓴 편지
        Letter letter = Letter.builder()
                .content("내용입니닷")
                .category(Category.CONSOLATION)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .writerId(10L)
                .build();

        //조회 시 편지
        when(letterRepository.findRandomLettersByCategory(category, 5)).thenReturn(List.of(letter));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        //when
        List<RandomLetterResponse> responses = letterMatchingService.findRandomLetters(category);
        RandomLetterResponse response = responses.get(0);
        // Then
        assertAll("랜덤 편지 응답 검증",
                () -> assertEquals("내용입니닷", response.getContent()),
                () -> assertEquals("12345", response.getZipCode()),
                () -> assertEquals(Category.CONSOLATION, response.getCategory()),
                () -> assertEquals(PaperType.BASIC, response.getPaperType()),
                () -> assertEquals(FontType.KYOBO, response.getFontType())
        );
    }

    @Test
    @DisplayName("findRandomLetters - 카테고리 null 시 전체 랜덤 편지 5개 조회 성공")
    void find_RandomLetters_WithNullCategory() {
        String category = "";

        // 10번 회원이 있고
        Member member = Member.builder()
                .zipCode("12345")
                .build();
        ReflectionTestUtils.setField(member, "id", 10L);

        // 10번 회원이 쓴 편지
        Letter letter = Letter.builder()
                .content("내용입니닷")
                .category(Category.ETC)
                .paperType(PaperType.BASIC)
                .fontType(FontType.KYOBO)
                .writerId(10L)
                .build();

        //조회 시 편지
        when(letterRepository.findRandomLetters(5)).thenReturn(List.of(letter));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        List<RandomLetterResponse> responses = letterMatchingService.findRandomLetters(category);
        RandomLetterResponse response = responses.get(0);

        assertAll("랜덤 편지 응답 검증",
                () -> assertEquals("내용입니닷", response.getContent()),
                () -> assertEquals("12345", response.getZipCode()),
                () -> assertEquals(Category.ETC, response.getCategory()),
                () -> assertEquals(PaperType.BASIC, response.getPaperType()),
                () -> assertEquals(FontType.KYOBO, response.getFontType())
        );
    }
}