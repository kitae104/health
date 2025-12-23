package kitae.spring.health.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kitae.spring.health.exceptions.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter  extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);    // "Bearer " 제거된 토큰

        if(token != null) {
            String email;

            try {
                email = jwtService.getUsernameFromToken(token);
            } catch (Exception e) {
                log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
                AuthenticationException authenticationException = new BadCredentialsException(e.getMessage());
                customAuthenticationEntryPoint.commence(request, response, authenticationException);
                return;
            }

            // 이메일로 UserDetails 로드
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // 이메일이 유효하고 토큰이 유효한 경우 SecurityContext에 인증 정보 설정
            // - StringUtils.hasText(email): 이메일이 null이거나 빈값인 경우를 방지
            // - jwtUtils.isTokenValid(...) : 토큰 서명/만료 등 검사
            if (StringUtils.hasText(email) && jwtService.isTokenValid(token, userDetails)) {
                log.info("유효한 Token, {}", email);

                // 인증 토큰 생성하여 권한과 함께 SecurityContext에 저장
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken); // 인증 정보 설정
            }
        }

        // 필터 체인 계속 진행
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 필터 처리 중 예외 발생 시 로깅 (응답 조작은 하지 않음)
            log.error("AuthFilter에서 예외 발생: " + e.getMessage());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String tokenWithBearer = request.getHeader("Authorization");
        if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")) {
            return tokenWithBearer.substring(7);
        }
        return null;
    }
}
