package com.example.backendproject01.service;

import com.example.backendproject01.dto.JoinRequest;
import com.example.backendproject01.dto.LoginRequest;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor // @RequiredArgsConstructor는 final 필드만 생성자에 포함, Spring이 생성자 호출 시 이미 주입완료
public class UserService {

    // final 써야하는 이유, 불변성(안전), nullpoint예외 방지(의존성주입 보장 - 생성자 생성)
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /** 회원가입 때 loginId 중복 체크, 중복 시 true return */
    public boolean checkLoginIdDuplicate(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    /** requset.toEntity()로 DTO(JoinRequest)에서 Entity(User)로 변환후 저장*/
    public void join(JoinRequest request){

        // 1. 중복 아이디 검사
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        userRepository.save(request.toEntity(bCryptPasswordEncoder.encode(request.getPassword())));
    }

    public User login(LoginRequest request) {

        // IllegalArgumentException은 입력값 검증에 특화된 구체적인 예외, RuntimeException은 넓은 범위라 잘 안씀
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw  new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;

    }


}
