package com.example.backendproject01.auth;

import com.example.backendproject01.entity.User;
import com.example.backendproject01.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 사용자 정보 로드 서비스
 * Security 는 User 객체가 아닌 PrincipalDetails 가 필요하기 때문에 따로 설정이 필요
 * => 이 서비스에서는 Security 에서 사용할 PrincipalDetails 를 return */
@Service
@RequiredArgsConstructor
public class PrincipalDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        // loadUserByUsername(loginId) → DB에서 사용자 조회 → PrincipalDetails 반환
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new PrincipalDetails(user);
    }

}
