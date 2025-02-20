package io.crops.warmletter.global.config;

import io.crops.warmletter.global.jwt.filter.JwtAuthenticationFilter;
import io.crops.warmletter.global.jwt.filter.JwtExceptionFilter;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import io.crops.warmletter.global.oauth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CorsConfig corsConfig;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                // 세션 설정(비활성화)
                .sessionManagement(
                        (sessionManagement) ->
                                sessionManagement.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS))
                // 요청에 대한 권한 설정
                .authorizeHttpRequests(
                        (authorizeRequests) ->
                                authorizeRequests
                                        .requestMatchers("/api/auth/**")
                                        .permitAll() // h2-console 접근 허용
                                        .requestMatchers("/swagger-ui/**")
                                        .permitAll() // Swagger UI 허용
                                        .requestMatchers("/api/bad-words/**").permitAll()
                                        .requestMatchers("/v3/api-docs/**")
                                        .permitAll() // API Docs 허용
                                        .anyRequest()
                                        .authenticated() // 그 외 요청은 인증 필요
                        )
                // OAuth2 설정 추가
                .oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService))
                        // 나중에 Handler 구현 후 추가될 부분
                        // .successHandler(oAuth2AuthenticationSuccessHandler)
                        // .failureHandler(oAuth2AuthenticationFailureHandler)
                ).addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtExceptionFilter(),
                        JwtAuthenticationFilter.class);

        return http.build();
    }

}
