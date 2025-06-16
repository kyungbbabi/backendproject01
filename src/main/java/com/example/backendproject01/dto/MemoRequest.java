package com.example.backendproject01.dto;

import com.example.backendproject01.entity.Memo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemoRequest {

    private String title;
    private String subtitle;
    private String content;

    public Memo toEntity() {
        return Memo.builder()
                .title(title)
                .subtitle(subtitle)
                .content(content)
                .build();
    }

}
