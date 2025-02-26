package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.enums.LetterEvaluation;
import io.crops.warmletter.domain.member.dto.response.MeResponse;
import io.crops.warmletter.domain.member.dto.response.ZipCodeResponse;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.TemperaturePolicy;
import io.crops.warmletter.domain.member.exception.DeletedMemberException;
import io.crops.warmletter.domain.member.exception.DuplicateZipCodeException;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthFacade authFacade;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ZIP_CODE_LENGTH = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void updateMemberEmail(Member member, String newEmail) {
        member.updateEmail(newEmail);
    }

    @Transactional
    public ZipCodeResponse createZipCode(HttpServletResponse response) {
        Long currentUserId = authFacade.getCurrentUserId();
        Member member = memberRepository.findById(currentUserId)
                                        .orElseThrow(MemberNotFoundException::new);

        // zipCode가 빈값인지 체크
        if (StringUtils.hasText(member.getZipCode())) {
            throw new DuplicateZipCodeException();
        }

        // 중복되지 않은 zipCode를 부여
        String zipCode;
        while (true) {
            zipCode = generateRandomString();

            if (!memberRepository.existsByZipCode(zipCode)) {
                member.updateZipCode(zipCode);
                break;
            }
        }

        // 새로운 accessToken 생성 및 헤더에 추가
        String newAccessToken = jwtTokenProvider.createAccessToken(
                member.getSocialUniqueId(),
                member.getRole(),
                zipCode,
                member.getId()
        );
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);

        return new ZipCodeResponse(zipCode);
    }

    private String generateRandomString() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < ZIP_CODE_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }

        return result.toString();
    }

    public MeResponse getMe() {
        Long currentUserId = authFacade.getCurrentUserId();

        return memberRepository.findMeById(currentUserId)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void deleteMe(String accessToken, String refreshToken, HttpServletResponse response) {
        Long currentUserId = authFacade.getCurrentUserId();
        Member member = memberRepository.findById(currentUserId)
                .orElseThrow(MemberNotFoundException::new);

        if (!member.isActive()) {
            throw new DeletedMemberException();
        }

        member.inactive();

        authFacade.logout(accessToken, refreshToken, response);
    }

    // 단독으로 사용되지 않으므로 @Transactional 어노테이션 미사용
    public void applyEvaluationTemperature(Long memberId, LetterEvaluation evaluation) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        if (evaluation == LetterEvaluation.GOOD) {
            member.applyTemperaturePolicy(TemperaturePolicy.GOOD_EVALUATION);
        } else if (evaluation == LetterEvaluation.BAD) {
            member.applyTemperaturePolicy(TemperaturePolicy.BAD_EVALUATION);
        }
    }
}
