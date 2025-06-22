package com.example.backendproject01.controller;

import com.example.backendproject01.auth.PrincipalDetails;
import com.example.backendproject01.dto.MemoRequest;
import com.example.backendproject01.dto.MemoResponse;
import com.example.backendproject01.entity.Memo;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.repository.MemoRepository;
import com.example.backendproject01.service.MemoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/memo")
@RequiredArgsConstructor
public class MemoApiController {

    private final MemoRepository memoRepository;
    private final MemoService memoService;

    /** 새 메모 작성 */
    @PostMapping
    public ResponseEntity<?> createMemo(@Valid @RequestBody MemoRequest memoRequest, BindingResult bindingResult, @AuthenticationPrincipal PrincipalDetails principal) {

        // 입력 검증 오류 처리
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", "입력값이 올바르지 않습니다."));
        }

        User currentUser = principal.getUser();
        MemoResponse response = memoService.createMemo(memoRequest, currentUser);
        return ResponseEntity.ok(response);

    }

    /** 전체 메모 목록 조회 (최신순) */
    public List<MemoResponse> getAllMemos() {
        List<Memo> memos = memoRepository.findAllByOrderByCreatedAtDesc();

        // Entity → DTO 변환 (권한 정보 없이)
        return memos.stream()
                .map(MemoResponse::from)
                .collect(Collectors.toList());
    }

    /** 특정 사용자의 메모 목록 조회 (최신순) */
    public List<MemoResponse> getMemosByUser(User user) {
        List<Memo> memos = memoRepository.findByAuthorOrderByCreatedAtDesc(user);

        return memos.stream()
                .map(memo -> MemoResponse.from(memo, user))
                .collect(Collectors.toList());
    }

    /** 특정 메모 조회 (ID 기준) - 메서드명 변경으로 혼동 방지 */
    public MemoResponse getMemoById(Long id, User currentUser) {
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다: " + id));

        // 권한 체크는 MemoResponse에서 처리
        return MemoResponse.from(memo, currentUser);
    }

    /** 전체 메모에서 키워드 검색 */
    public List<MemoResponse> searchMemos(String keyword) {

        // 키워드가 없으면 전체 목록 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMemos();
        }
        List<Memo> memos = memoRepository.findByKeyword(keyword.trim());

        return memos.stream()                   // List<Memo>를 Stream<Memo>로 변환
                .map(MemoResponse::from)        // 각 Memo 객체를 MemoResponse 객체로 변환 (메서드 참조 사용)
                .collect(Collectors.toList());  // Stream을 다시 List로
    }

    /** 메모 수정 */
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

    /** 메모 삭제 */
    @Transactional
    public void deleteMemo(Long id, User currentUser) {
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다: " + id));

        if (!memo.canBeDeletedBy(currentUser)) {
            throw new IllegalArgumentException("메모를 삭제할 권한이 없습니다.");
        }

        memoRepository.deleteById(id);
    }

    /** 특정 사용자의 메모 개수 조회 */
    public long getMemoCountByUser(User user) {
        return memoRepository.countByAuthor(user);
    }
}