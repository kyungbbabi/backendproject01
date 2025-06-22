package com.example.backendproject01.dto;

import com.example.backendproject01.entity.Memo;
import com.example.backendproject01.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Memo 요청 DTO - 1. Validation 어노테이션 추가, 2. toEntity 메서드에서 User 매개변수 받기 */
@Getter
@Setter
@NoArgsConstructor
public class MemoRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    private String subtitle;
    private String content;

    /** DTO → Entity 변환, User 정보를 포함한 완전한 Entity 생성 */
    public Memo toEntity(User author) {
        return Memo.builder()
                .title(title)
                .subtitle(subtitle)
                .content(content)
                .author(author)
                .build();
    }

}
