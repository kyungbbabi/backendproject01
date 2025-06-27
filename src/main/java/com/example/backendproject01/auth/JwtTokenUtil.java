package com.example.backendproject01.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰 생성
 *
 * PUBLIC 메서드: 다른 클래스가 실제로 호출해야 하는 기능
 * generateToken() -> 로그인 시 사용
 * validateToken() -> JwtRequestFilter 에서 인증 시 사용
 * getUserIdFromToken() -> Controller 에서 현재 사용자 식별 시 사용
 *
 * PRIVATE 메서드: 클래스 내부의 구현 세부사항
 * getAllClaimsFromToken() -> JWT 파싱 로직 (외부 노출 불필요)
 * createToken() -> 토큰 생성 로직 (generateToken()이 안전한 인터페이스 제공)
 * isTokenExpired() -> 부분 검증보다는 validateToken()의 종합 검증 권장
 *
 * */
@Component
public class JwtTokenUtil {

    // JWT 서명용 비밀키 (HS512 알고리즘용 키 자동 생성), 매번 새로운 랜덤 시크릿 키가 생성되어서 토큰 생성 시의 키와 검증 시의 키가 달라지기에, 고정키 값이 필요함
    // private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    /**
     * 사용자 정보를 바탕으로 JWT 토큰 생성
     * 1. UserDetails에서 기본 정보 추출
     * 2. PrincipalDetails 인 경우 User 엔티티의 추가 정보 추출(userId, name), 왜? JWT 는 Stateless(서버가 기억을 안한다.) 필요한 모든 정보를 토큰에 포함해야 함
     * => 서버가 세션을 기억하지 않기 때문에, 클라이언트가 들고 다니는 토큰(JWT)에 userId, name 같은 사용자 정보를 포함시켜야 한다는 의미
     * 3. JWT 토큰 생성 및 반환\
     *
     * */
    public String generateToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof PrincipalDetails) { // userDetails 객체가 PrincipalDetails 타입이면 true, 아니면 false를 반환.
            PrincipalDetails principalDetails = (PrincipalDetails) userDetails;
            claims.put("userId", principalDetails.getUser().getId());
            claims.put("name", principalDetails.getUser().getName());
        }

        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 실제 JWT 토큰 생성 로직 (ex.)
     * {
     *   "sub": "user123",           // subject (사용자 식별자)
     *   "userId": 1,                // 커스텀 클레임
     *   "name": "홍길동",             // 커스텀 클레임
     *   "iat": 1640995200,          // issued at (발급 시간)
     *   "exp": 1641081600           // expiration (만료 시간)
     * }
     */
    private String createToken(Map<String, Object> claims, String subject){

        return Jwts.builder()
                .setClaims(claims)      // 사용자 정의 데이터
                .setSubject(subject)    // 주체 (loginId)
                .setIssuedAt(new Date(System.currentTimeMillis()))                      // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))    // 만료 시간, 86400(1시간)
                .signWith(getSigningKey())    // 서명
                .compact();             // 문자열로 변환

    }

    /** JWT 토큰을 Parsing 하여 모든 클레임 정보 추출, 서명 유효성 (getAllClaimsFromToken 에서 자동 검증) */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())     // 서명 검증용 키 설정
                .build()
                .parseClaimsJws(token)              // 토큰 파싱 및 서명 검증
                .getBody();                         // Claims 정보 반환
    }

    /** JWT 토큰에서 특정 클레임(정보) 추출하는 범용 메서드 */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /** JWT 토큰에서 사용자명(loginId) 추출, 토큰의 subject 필드에 저장된 값을 가져옴*/
    public String getUsernameFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            return null;    // token 유효하지 않는 경우(만료, 손상, 형식 오류 등) null 반환
        }
    }

    /** JWT 토큰에서 만료 시간 추출 */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /** JWT 토큰 만료 여부 확인, 만료 시간이 없는 경우 (기존 토큰) 만료된 것으로 처리 */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return true;
        }
        return expiration.before(new Date());
    }

    /** JWT 토큰이 유효한지 검증 */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            // 토큰에서 추출한 사용자명과 UserDetails 의 사용자명 일치 여부 ,토큰 만료 여부, 서명 유효성 (getAllClaimsFromToken 에서 자동 검증)
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            // JWT 토큰이 유효하지 않은 경우 (서명 불일치, 형식 오류 등)
            return false;
        }
    }

    /** JWT 토큰에서 사용자 ID 추출, API 에서 현재 로그인한 사용자 식별용 */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /** JWT 토큰이 유효한지 기본 검증 (JwtRequestFilter용) */
    public boolean isTokenValid(String token) {
        try {
            // 토큰 파싱 시도
            getAllClaimsFromToken(token);
            // 만료 여부 확인
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
