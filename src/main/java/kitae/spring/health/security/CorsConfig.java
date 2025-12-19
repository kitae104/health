package kitae.spring.health.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS 설정 클래스
// - 웹 브라우저의 동일 출처 정책(SOP)으로 인해 발생하는 교차 출처 요청을 허용하도록 설정
// - 개발/테스트 편의를 위해 모든 출처(*)를 허용하고 있으나, 운영 환경에서는 필요한 출처만 허용하도록 제한 권장
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("*"); // 신뢰할 수 있는 프런트엔드에서 제공하는 접속만 허용
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    // WebMvcConfigurer 빈을 통해 전역 CORS 매핑을 설정
    // - 모든 경로("/**")에 대해 GET, POST, PUT, DELETE 메서드를 허용
    // - allowedOrigins("*")로 모든 출처를 허용(운영 환경에서는 구체적 도메인을 명시하세요)
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedOrigins("*");
            }
        };
    }
}