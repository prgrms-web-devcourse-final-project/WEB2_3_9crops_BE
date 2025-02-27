package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.config.TestConfig;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.dto.request.ApproveLetterRequest;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.repository.LetterTemporaryMatchingRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
@SpringBootTest
public class RandomLetterServiceConcurrencyTest {

    @Autowired
    private RandomLetterService randomLetterService;

    @Autowired
    private LetterTemporaryMatchingRepository letterTemporaryMatchingRepository;

    @Autowired
    private LetterRepository letterRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private AuthFacade authFacade;

    private Member savedMember; //회원 1 생성
    private Letter savedLetter; //저장된 편지 생성


    @BeforeEach
    void setupAuth() {
        savedMember = memberRepository.save(Member.builder()
                .email("test@example.com")
                .password("password")
                .socialUniqueId("unique_test_id")
                .role(Role.USER)
                .build());

        savedLetter = letterRepository.save(Letter.builder()
                .writerId(20L)                   // ApproveLetterRequest의 writerId와 일치
                .receiverId(savedMember.getId())   // 현재 사용자의 아이디
                .parentLetterId(null)
                .letterType(LetterType.RANDOM)
                .category(Category.ETC)
                .title("Test Letter")
                .content("Test Content")
                .status(Status.IN_DELIVERY)
                .fontType(FontType.DEFAULT)
                .paperType(PaperType.BASIC)
                .build());

        when(authFacade.getCurrentUserId()).thenReturn(savedMember.getId());
        when(authFacade.getZipCode()).thenReturn("12345");
    }

    @BeforeEach
    void setup() {
        // 테스트를 위해 임시 매칭 테이블 초기화
        letterTemporaryMatchingRepository.deleteAll();

    }

    @Test
    @DisplayName("동시성 이슈 생성 - 동시에 100명 요청 시 단 한 건의 승인만 성공해야 함 (유니크 제약 조건으로 해결)")
    void approveLetter_concurrentRequests_onlyOneApprovalSucceeds() throws Exception {
        // given
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        ApproveLetterRequest request = ApproveLetterRequest.builder()
                .letterId(savedLetter.getId())
                .writerId(20L)
                .build();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    randomLetterService.approveLetter(request);
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // 검증: 예약 테이블에 단 하나의 레코드만 생성되었어야 함
        List<LetterTemporaryMatching> matches = letterTemporaryMatchingRepository.findAll();
        assertEquals(1, matches.size(), "동시 승인 호출 시 단 한 건의 매칭만 생성되어야 합니다.");

        LetterTemporaryMatching approved = matches.get(0);
        assertEquals(request.getLetterId(), approved.getLetterId());
        assertEquals(request.getWriterId(), approved.getFirstMemberId()); //최초로 편지쓴 사람 id = 편지 작성자인지 확인
    }

}
