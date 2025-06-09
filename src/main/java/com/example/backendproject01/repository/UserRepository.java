package com.example.backendproject01.repository;

import com.example.backendproject01.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
