package com.example.backendproject01.repository;

import com.example.backendproject01.entity.Memo;
import com.example.backendproject01.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    List<Memo> findAllByOrderByCreatedAtDesc(); // 최신 순으로 조회
    List<Memo> findByAuthorOrderByCreatedAtDesc(User author);   // 특정 사용자의 메모를 최신순으로 조회
    List<Memo> findByTitleOrderByCreatedAtDesc(String title);   // 제목으로 검색

    Optional<Memo> findByIdAndAuthor(Long id, User author);     // 사용자가 작성한 메모 조회 (권한 체크용)

    long countByAuthor(User author);    // 특정 사용자의 메모 개수 조회

    // 제목 또는 내용으로 검색
    @Query("SELECT m FROM Memo m WHERE m.title LIKE %:keyword% OR m.content LIKE %:keyword% ORDER BY m.createdAt DESC ")
    List<Memo> findByKeyword(String keyword);

}
