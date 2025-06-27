package com.example.backendproject01.service;

import com.example.backendproject01.dto.MemoRequest;
import com.example.backendproject01.dto.MemoResponse;
import com.example.backendproject01.entity.Memo;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.repository.MemoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;

    public MemoResponse createMemo(MemoRequest request, User author) {
        Memo memo = request.toEntity(author);           // DTO → Entity 변환
        Memo savedMemo = memoRepository.save(memo);     // DB 저장

        return MemoResponse.from(savedMemo, author);    // Entity → DTO 변환 후 반환
    }

    /** 전체 메모 목록 조회 (최신순) */
    public List<MemoResponse> getAllMemos() {
        List<Memo> memos = memoRepository.findAllByOrderByCreatedAtDesc();

        // Entity → DTO 변환 (권한 정보 없이)
        return memos.stream()
                .map(MemoResponse::from)
                .collect(Collectors.toList());
    }

    /** 특정 메모 조회 (ID 기준) */
    public MemoResponse getMemoById(Long id, User currentUser) {
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다: " + id));
        return MemoResponse.from(memo, currentUser);
    }

    /** 특정 사용자의 메모 목록 조회 (최신순) */
    public List<MemoResponse> getMemosByUser(User user) {
        List<Memo> memos = memoRepository.findByAuthorOrderByCreatedAtDesc(user);

        return memos.stream()
                .map(memo -> MemoResponse.from(memo, user))
                .collect(Collectors.toList());
    }

    /** 전체 메모에서 키워드 검색 */
    public List<MemoResponse> searchMemos(String keyword) {
        // 키워드가 없으면 전체 목록 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMemos();
        }
        List<Memo> memos = memoRepository.findByKeyword(keyword.trim());

        return memos.stream()
                .map(MemoResponse::from)
                .collect(Collectors.toList());
    }

    /** 특정 사용자의 메모에서 키워드 검색 */
    public List<MemoResponse> searchMyMemos(String keyword, User user) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getMemosByUser(user);
        }

        // 사용자별 검색을 위한 Repository 메서드 사용
        List<Memo> memos = memoRepository.findByKeywordAndAuthor(keyword.trim(), user);

        return memos.stream()
                .map(memo -> MemoResponse.from(memo, user))
                .collect(Collectors.toList());
    }

    @Transactional
    public MemoResponse updateMemo(Long id, MemoRequest request, User currentUser) {
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다: " + id));

        if (!memo.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("메모를 수정할 권한이 없습니다.");
        }

        memo.updateMemo(request.getTitle(), request.getSubtitle(), request.getContent());
        Memo updateMemo = memoRepository.save(memo);

        return MemoResponse.from(updateMemo, currentUser);
    }

    @Transactional
    public void deleteMemo(Long id, User currentUser) {
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다: " + id));

        if (!memo.canBeDeletedBy(currentUser)) {
            throw new IllegalArgumentException("메모를 삭제할 권한이 없습니다.");
        }

        memoRepository.deleteById(id);
    }

}