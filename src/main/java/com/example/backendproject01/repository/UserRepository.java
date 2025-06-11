package com.example.backendproject01.repository;

import com.example.backendproject01.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Optional User 객체가 있을 수도 있고, 없을 수도 있다, Spring Data JPA가 메서드 이름을 분석해서 자동으로 쿼리를 생성
    // findByLoginId(String loginId)     -> SELECT * FROM user WHERE login_id = ?
    // existsByLoginId(String loginId)   -> SELECT COUNT(*) > 0 FROM user WHERE login_id = ?
    Optional<User> findByLoginId(String loginId);  // 실제 User 객체가 필요함 - 비밀번호 비교를 해야 하니까!, loginId 필드를 기준으로!, 로그인뿐 아니라 정보 조회/수정에도 필요(데이터 전부 필요하니까!)
    boolean existsByLoginId(String loginId); // 중복검사 - 존재의 여부만, 실제 User 객체는 안 가져옴 - 가볍다

}
