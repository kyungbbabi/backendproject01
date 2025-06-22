package com.example.backendproject01.auth;

import com.example.backendproject01.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
우리가 직접 로그인 처리를 안해도 되는 대신 지정해줘야 할 정보들 (ex.  /login 에 대한 요청을 security가 가로채서 로그인 진행, @PostMapping("/login") 을 만들지 않아도 됨)
로그인에 성공 시 Security Session을 생성(Key값 : Security ContextHolder)
Security Session(Authentication(UserDetails)) 이런 식의 구조로 되어있는데 PrincipalDetails에서 UserDetails를 설정해준다고 보면 됨
*/

public class PrincipalDetails implements UserDetails {

    private final User user;
    public PrincipalDetails(User user) {
        this.user = user;
    }

    //
    public User getUser() {
        return user;
    }

    /** 권한 관련 작업, 여기서는 admin 이 없음*/
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    // get Password 메서드
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // get Username 메서드()
    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    // 계정이 만료 되었는지 (true: 만료 x)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겼는지 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호가 만료 되었는지 (true: 만료x)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화가 되어있는지 (true: 활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }

}
