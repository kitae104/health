package kitae.spring.health.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    // application.properties에서 주입받는 만료 시간 (현재 3개월)
    @Value("${jwt.expiration.time}")
    private long expirationTime;

    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    // HmacSHA256 서명을 만들기 위한 SecretKey 객체
    private SecretKey key;

    // 빈 초기화 시 SecretKey 생성
    @PostConstruct
    private void init() {
        // 1) Base64 디코드 → 2) 길이 검증 포함한 키 생성
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretBase64);
        this.key = Keys.hmacShaKeyFor(keyBytes); // HS256/384/512용 적절한 길이 확인

        // 선택: HS256 기준 32바이트(=256bit) 미만이면 명확한 에러 주기
        if (key.getEncoded().length < 32) {
            throw new IllegalStateException("JWT secret too short: need >= 32 bytes for HS256");
        }
    }

    // 주어진 이메일(또는 사용자 식별자)로 JWT를 생성하여 반환
    // 토큰에는 subject, issuedAt, expiration을 설정하고, SecretKey로 서명함
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰에서 subject(이 예제에서는 이메일)를 추출하여 반환
    public String getUsernameFromToken(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // 토큰을 파싱하여 Claims를 추출한 뒤, 호출자가 전달한 함수(claimsTFunction)를 적용해 결과를 반환
    // 예: Claims::getSubject, Claims::getExpiration 등을 사용할 수 있음
    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(
                Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
        );
    }

    // 토큰이 주어진 UserDetails의 사용자와 일치하고 만료되지 않았는지 검사
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 토큰 만료 여부 검사 (만료일이 현재보다 이전이면 만료로 판단)
    private boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
}