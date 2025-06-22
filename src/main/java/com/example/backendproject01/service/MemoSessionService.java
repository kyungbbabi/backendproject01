package com.example.backendproject01.service;

import com.example.backendproject01.dto.MemoRequest;
import com.example.backendproject01.entity.Memo;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.repository.MemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoSessionService {

    private final MemoRepository memoRepository;

    /** 메모 정렬(최신순) */
    public List<Memo> getAllMemo() {
        return memoRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 메모 제목으로 찾기 */
    public List<Memo> searchMemoByTitle(String title) {
        return memoRepository.findByTitleOrderByCreatedAtDesc(title);
    }

    /** 메모 keyword로 찾기 */
    public List<Memo> searchMemoByKeyword(String keyword) {
        return memoRepository.findByKeyword(keyword);
    }

    /** 새 메모 작성 */
    public Memo createMemo(MemoRequest request, User author) {
        return memoRepository.save(request.toEntity(author));
    }

    /** 메모 세부내용 */
    public Memo readMemoById(Long id) {
        return memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다."));
    }

    /** 메모 수정 */
    public Memo updateMemo(Long id, MemoRequest request) {

        Memo memo = readMemoById(id);
        memo.updateMemo(request.getTitle(), request.getSubtitle(), request.getContent());

        return memoRepository.save(memo);

    }

    /** 메모 삭제 */
    public void deleteMemo(Memo memo) {
        memoRepository.delete(memo);
    }





}
