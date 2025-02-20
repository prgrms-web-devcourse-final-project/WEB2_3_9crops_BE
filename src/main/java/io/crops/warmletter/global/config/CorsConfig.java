package io.crops.warmletter.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:3000", // 로컬 프론트엔드
                        "http://localhost:8080", // 개발 테스트
                        "https://your-domain.com" // 운영 프론트엔드
                ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Cache-Control", "x-requested-with"));

        // 노출할 헤더 설정 추가
        configuration.setExposedHeaders(List.of("Authorization"));

        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // preflight 요청의 캐시 시간 (1시간)
        // API 요청 시 서버는 OPTIONS 메서드를 통해 제공하는 메서드인지를 먼저 체크한다.
        // 캐싱을 통해 불필요한 preflight 요청을 줄여서 성능을 개선
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
