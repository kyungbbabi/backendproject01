package com.example.backendproject01.controller;

import com.example.backendproject01.auth.PrincipalDetails;
import com.example.backendproject01.dto.MemoRequest;
import com.example.backendproject01.dto.MemoResponse;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.service.MemoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memo")
@RequiredArgsConstructor
public class MemoApiController {

    private final MemoService memoService;

    /** 새 메모 작성 */
    @PostMapping
    public ResponseEntity<?> createMemo(@Valid @RequestBody MemoRequest memoRequest, BindingResult bindingResult, @AuthenticationPrincipal PrincipalDetails principal) {

        // 입력 검증 오류 처리
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", "입력값이 올바르지 않습니다."));
        }

        try {
            User currentUser = principal.getUser();
            MemoResponse response = memoService.createMemo(memoRequest, currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 메모 목록 조회 (검색 기능 포함) */
    @GetMapping
    public ResponseEntity<List<MemoResponse>> getMemos(@RequestParam(required = false) String keyword) {
        try {
            List<MemoResponse> memos;

            // 키워드가 있으면 검색, 없으면 전체 조회
            if (keyword != null && !keyword.trim().isEmpty()) {
                memos = memoService.searchMemos(keyword.trim());
            } else {
                memos = memoService.getAllMemos();
            }

            return ResponseEntity.ok(memos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** 현재 사용자의 메모 목록 조회 (검색 기능 포함) */
    @GetMapping("/my")
    public ResponseEntity<List<MemoResponse>> getMyMemos(@RequestParam(required = false) String keyword, @AuthenticationPrincipal PrincipalDetails principal) {
        try {
            User currentUser = principal.getUser();
            List<MemoResponse> memos;

            if (keyword != null && !keyword.trim().isEmpty()) {
                memos = memoService.searchMyMemos(keyword.trim(), currentUser);
            } else {
                memos = memoService.getMemosByUser(currentUser);
            }

            return ResponseEntity.ok(memos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** 특정 메모 조회 (ID 기준) */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMemoById(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails principal) {
        try {
            User currentUser = principal.getUser();
            MemoResponse memo = memoService.getMemoById(id, currentUser);
            return ResponseEntity.ok(memo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "메모 조회에 실패했습니다."));
        }
    }

    /** 메모 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMemo(@PathVariable Long id, @Valid @RequestBody MemoRequest request, BindingResult bindingResult, @AuthenticationPrincipal PrincipalDetails principal) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", "입력값이 올바르지 않습니다."));
        }

        try {
            User currentUser = principal.getUser();
            MemoResponse response = memoService.updateMemo(id, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "메모 수정에 실패했습니다."));
        }
    }

    /** 메모 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMemo(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails principal) {
        try {
            User currentUser = principal.getUser();
            memoService.deleteMemo(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "메모가 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "메모 삭제에 실패했습니다."));
        }
    }

}