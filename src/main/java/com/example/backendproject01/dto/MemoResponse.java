package com.example.backendproject01.dto;

import com.example.backendproject01.entity.Memo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Memo 응답 DTO - 1. Entity의 민감한 정보 숨김, 2. 클라이언트 요구사항에 맞는 데이터 구조 제공, 3. API 버전별 응답 가능  */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemoResponse {

    private Long id;
    private String title;
    private String subtitle;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String formattedCreatedAt;
    private String formattedUpdatedAt;

    private AuthorInfo author;          // 작성자 정보 (민감한 정보 제외)
    private PermissionInfo permissions; // 권한 정보

    @Getter
    @AllArgsConstructor
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PermissionInfo {
        private boolean canEdit;
        private boolean canDelete;
    }

    /** Entity → DTO 변환 (현재 사용자 기준)
     * static을 사용하는 이유:
     *
     * 객체 생성 없이 호출 가능: MemoResponse.from(memo, user) 형태로 직접 호출
     * 팩토리 메서드 패턴: 객체 생성의 복잡한 로직을 캡슐화
     * 메모리 효율성: 인스턴스 변수에 접근하지 않으므로 static이 적합
     * 의미적 명확성: "변환 기능"임을 명확히 표현
     * */
    public static MemoResponse from(Memo memo, com.example.backendproject01.entity.User currentUser) {
        return MemoResponse.builder()
                .id(memo.getId())
                .title(memo.getTitle())
                .subtitle(memo.getSubtitle())
                .content(memo.getContent())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .formattedCreatedAt(memo.getFormattedCreatedAt())
                .formattedUpdatedAt(memo.getFormattedUpdatedAt())
                .author(AuthorInfo.builder()
                        .id(memo.getAuthor().getId())
                        .name(memo.getAuthor().getName())
                        .build())
                .permissions(PermissionInfo.builder()
                        .canEdit(memo.canBeEditedBy(currentUser))
                        .canDelete(memo.canBeDeletedBy(currentUser))
                        .build())
                .build();
    }

    /** Entity → DTO 변환 (권한 체크 없음 - 목록 조회용) */
    public static MemoResponse from(Memo memo) {
        return MemoResponse.builder()
                .id(memo.getId())
                .title(memo.getTitle())
                .subtitle(memo.getSubtitle())
                .content(memo.getContent())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .formattedCreatedAt(memo.getFormattedCreatedAt())
                .formattedUpdatedAt(memo.getFormattedUpdatedAt())
                .author(AuthorInfo.builder()
                        .id(memo.getAuthor().getId())
                        .name(memo.getAuthor().getName())
                        .build())
                .build();
    }

}
