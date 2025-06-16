package com.example.backendproject01.repository;

import com.example.backendproject01.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    // 최신 순으로 조회
    List<Memo> findAllByOrderByCreatedAtDesc();

    // 제목으로 검색
    List<Memo> findByTitleOrderByCreatedAtDesc(String title);

    // 제목 또는 내용으로 검색
    List<Memo> findByKeyword(String keyword);

}
