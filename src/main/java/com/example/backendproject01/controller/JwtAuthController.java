package com.example.backendproject01.controller;

import com.example.backendproject01.auth.JwtTokenUtil;
import com.example.backendproject01.auth.PrincipalDetailService;
import com.example.backendproject01.auth.PrincipalDetails;
import com.example.backendproject01.dto.JoinRequest;
import com.example.backendproject01.dto.LoginRequest;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JwtAuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager; // 사용자 인증
    private final JwtTokenUtil jwtTokenUtil;
    private final PrincipalDetailService principalDetailService;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody JoinRequest joinRequest, BindingResult bindingResult) {

        if (userService.checkLoginIdDuplicate(joinRequest.getLoginId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "로그인 아이디가 중복됩니다."));
        }
        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        try {
            userService.join(joinRequest);
            return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        try {
            // Spring Security를 통한 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getLoginId(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();  // 인증된 사용자 정보 추출
            String token = jwtTokenUtil.generateToken(userDetails);                 // JWT 토큰 생성

            // 사용자 정보 추출 (PrincipalDetails인 경우)
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");

            if (userDetails instanceof PrincipalDetails) {
                PrincipalDetails principalDetails = (PrincipalDetails) userDetails;
                User user = principalDetails.getUser();
                response.put("user", Map.of( "id", user.getId(), "loginId", user.getLoginId(), "name", user.getName()));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "로그인에 실패했습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error","유효하지 않은 토큰 형식입니다."));
            }

            // 토큰이 기본적으로 유효한지 확인 (만료는 허용)
            String jwtToken = token.substring(7);
            String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            if (username == null) {
                return ResponseEntity.badRequest().body(Map.of("error","유효하지 않은 토큰입니다."));
            }

            UserDetails userDetails = principalDetailService.loadUserByUsername(username);  // 사용자 정보 조회
            String newToken = jwtTokenUtil.generateToken(userDetails);                      // 새 토큰 생성

            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("type", "Bearer");

            if (userDetails instanceof PrincipalDetails) {
                PrincipalDetails principalDetails = (PrincipalDetails) userDetails;
                User user = principalDetails.getUser();
                response.put("user", Map.of(
                        "id", user.getId(),
                        "loginId", user.getLoginId(),
                        "name", user.getName()
                ));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "토큰 갱신에 실패했습니다."));
        }
    }
}
