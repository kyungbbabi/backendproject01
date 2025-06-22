package com.example.backendproject01.controller;

import com.example.backendproject01.auth.JwtTokenUtil;
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
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

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
            // Spring Security를 통한 인증
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
            String jwtToken = token.substring(7);                    // Bearer 토큰에서 실제 토큰 추출
            String username = jwtTokenUtil.getUsernameFromToken(jwtToken);      // 토큰에서 사용자명 추출

            // 새 토큰 생성을 위해 사용자 정보 조회 필요
            // 이 부분은 PrincipalDetailService를 통해 처리해야 함

            return ResponseEntity.ok(Map.of("message", "토큰 갱신 기능은 추후 구현 예정"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "토큰 갱신에 실패했습니다."));
        }
    }
}
