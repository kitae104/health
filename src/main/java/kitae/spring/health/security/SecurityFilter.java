package kitae.spring.health.security;

import kitae.spring.health.exceptions.CustomAccessDenialHandler;
import kitae.spring.health.exceptions.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityFilter {

    // JWT 인증 필터 (요청마다 토큰 검사 및 SecurityContext 설정)
    private final AuthFilter authFilter;

    // 인증 실패 시 응답 처리를 담당하는 엔트리 포인트
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // 권한 부족(Access Denied) 상황에서의 응답 처리기
    private final CustomAccessDenialHandler customAccessDenialHandler;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    /**
     * SecurityFilterChain 빈 등록
     * - CSRF 비활성화, CORS 기본설정, 예외 처리 핸들러 등록
     * - '/api/auth/**' 경로는 인증 불필요하도록 허용, 그 외 경로는 모두 인증 필요
     * - 세션을 사용하지 않는 STATLESS 정책 적용 (JWT 등 토큰 기반 인증에 적합)
     * - 커스텀 AuthFilter를 UsernamePasswordAuthenticationFilter 이전에 배치
     * @param httpSecurity
     * @return
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .exceptionHandling(exception -> exception
                .accessDeniedHandler(customAccessDenialHandler)
                .authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .authorizeHttpRequests(request -> request
                .requestMatchers("/api/auth/**", "/api/doctors/**").permitAll()
//                .requestMatchers("/api/roles/**").permitAll()
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(manager -> manager
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈 (BCrypt 사용)
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈 등록 (기본 AuthenticationConfiguration에서 가져옴)
     * 컨트롤러나 서비스에서 직접 인증을 수행해야 할 때 주입해서 사용
     * @param authenticationConfiguration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
