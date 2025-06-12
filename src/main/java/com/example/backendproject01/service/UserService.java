package com.example.backendproject01.service;

import com.example.backendproject01.dto.JoinRequest;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /** 회원가입 때 loginId 중복 체크, 중복 시 true return */
    public boolean checkLoginIdDuplicate(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    /** requset.toEntity()로 DTO(JoinRequest)에서 Entity(User)로 변환후 저장*/
    public void join(JoinRequest request){
        userRepository.save(request.toEntity(bCryptPasswordEncoder.encode(request.getPassword())));
    }




}
