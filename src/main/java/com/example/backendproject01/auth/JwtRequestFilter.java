package com.example.backendproject01.auth;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** API 요청 시 JWT 토큰 검증, HTTP 헤더의 Authorization: Bearer {token}을 수동 검증 필요 */
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final PrincipalDetailService principalDetailService;    // 사용자 정보 조회용 (JWT 에서 추출한 loginId로 DB 조회)
    private final JwtTokenUtil jwtTokenUtil;                        // JWT 토큰 파싱 및 검증용

    /** 모든 HTTP 요청에 대해 실행되는 필터 메서드, OncePerRequestFilter 상속 -> 요청당 한 번만 실행 보장 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // HTTP 헤더에서 Authorization 정보 추출
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // Bearer 토큰 형식인지 확인 및 토큰 추출
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7); // "Bearer " 제거
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken); // JwtToken 사용자명(LoginId) 추출
            }   catch (IllegalArgumentException e) {
                logger.error("JWT Token을 가져올 수 없습니다", e);
            }   catch (ExpiredJwtException e) {
                logger.error("Jwt Token이 만료되었습니다.", e);     // 만료된 토큰의 경우 401 Unauthorized 응답은 JwtAuthenticationEntryPoint에서 처리
            }   catch (Exception e) {
                logger.error("Jwt parsing error", e);
            }
        }

        // 사용자명이 추출되었고, 아직 인증되지 않은 경우에만 처리
        // SecurityContextHolder.getContext().getAuthentication() == null -> 현재 요청에서 아직 인증이 설정되지 않았음을 의미
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // DB에서 사용자 정보 조회, JWT는 Stateless이므로 사용자 권한 변경 등을 실시간 반영하기 위해
            UserDetails userDetails = principalDetailService.loadUserByUsername(username);

            // JWT 토큰과 DB의 사용자 정보를 비교하여 유효성 검증
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                // 인증 토큰 생성 (Spring Security 내부 객체), UsernamePasswordAuthenticationToken: Spring Security 인증 정보 저장용 객체
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                    // principal: 인증된 사용자 정보
                                null,                           // credentials: 비밀번호 (JWT 불필요)
                                userDetails.getAuthorities()    // authorities: 사용자 권한 목록
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));   // 요청 세부 정보 설정 (IP, 세션 등)
                // SecurityContext 인증 정보 설정, Controller 에서 @AuthenticationPrincipal 로 사용자 정보 접근 가능
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}
