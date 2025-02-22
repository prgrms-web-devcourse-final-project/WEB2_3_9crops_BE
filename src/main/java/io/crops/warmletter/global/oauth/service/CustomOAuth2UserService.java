package io.crops.warmletter.global.oauth.service;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.entity.SocialAccount;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.enums.SocialProvider;
import io.crops.warmletter.domain.member.exception.DeletedMemberException;
import io.crops.warmletter.domain.member.facade.MemberFacade;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import io.crops.warmletter.global.oauth.exception.OAuth2EmailNotFoundException;
import io.crops.warmletter.global.oauth.exception.OAuth2ProcessingException;
import io.crops.warmletter.global.oauth.userinfo.OAuth2UserInfo;
import io.crops.warmletter.global.oauth.userinfo.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberFacade memberFacade;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return this.process(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("OAuth2 인증 처리 중 에러 발생", ex);
            throw new OAuth2ProcessingException();
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oauth2User.getAttributes()
        );

        String socialUniqueId = provider.name() + "_" + userInfo.getId();

        String email = userInfo.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new OAuth2EmailNotFoundException(registrationId);
        }

        Optional<Member> memberOptional = memberRepository.findBySocialUniqueId(socialUniqueId);

        Member member;
        if (memberOptional.isEmpty()) {
            // 새 회원 가입
            member = Member.builder()
                    .email(email)
                    .socialUniqueId(socialUniqueId)
                    .role(Role.USER)
                    .temperature(36.5f)  // 기본 온도 설정
                    .build();

            SocialAccount socialAccount = SocialAccount.builder()
                    .provider(provider)
                    .socialId(userInfo.getId())
                    .build();

            socialAccount.setMember(member);
            member.getSocialAccounts().add(socialAccount);

            member = memberRepository.save(member);
            log.info("새로운 소셜 계정으로 회원가입이 완료되었습니다. email: {}, provider: {}", email, registrationId);
        } else {
            member = memberOptional.get();

            // 탈퇴한 회원인지 확인
            if (!member.isActive()) {
                throw new DeletedMemberException();
            }

            // 이메일이 변경되었을 경우 업데이트
            if (!member.getEmail().equals(email)) {
                String oldEmail = member.getEmail();
                memberFacade.updateMemberEmail(member, email);
                log.info("소셜 계정의 이메일이 변경되었습니다. old: {}, new: {}, provider: {}",
                        oldEmail, email, registrationId);
            } else {
                log.info("기존 소셜 계정으로 로그인했습니다. email: {}, provider: {}", email, registrationId);
            }
        }

        return UserPrincipal.create(member, oauth2User.getAttributes());
    }

}
