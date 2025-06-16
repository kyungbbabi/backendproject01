package com.example.backendproject01.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String subtitle;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFormattedCreatedAt(){
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    public String getFormattedUpdatedAt(){
        return updatedAt.format(DateTimeFormatter.ofPattern("yyyy/MM//dd"));
    }

    public void updateMemo(String title, String subtitle, String content) {
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
    }

}
