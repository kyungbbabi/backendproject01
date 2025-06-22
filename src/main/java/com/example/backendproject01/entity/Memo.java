package com.example.backendproject01.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing 활성화 (생성/수정 시간 자동 관리)
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String subtitle;
    private String content;


    @ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩: 연관 데이터를 실제 사용할 때만 DB에서 가져와서 성능 최적화, N:1
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // JPA Auditing으로 자동 관리
    @CreatedDate                // 생성 시 자동으로 현재 시간 설정
    @Column(updatable = false)  // 수정 불가능
    private LocalDateTime createdAt;

    @LastModifiedDate           // 수정 시 자동으로 현재 시간 설정
    private LocalDateTime updatedAt;

    public String getFormattedCreatedAt(){
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }
    public String getFormattedUpdatedAt(){
        return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }

    /** 비즈니스 메서드 - 메모 내용 수정, updatedAt은 JPA Auditing이 자동 처리 */
    public void updateMemo(String title, String subtitle, String content) {
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
    }

    // 비즈니스 메서드 - 권한 체크
    public boolean isOwnedBy(User user) {
        return this.author != null && this.author.getId().equals(user.getId());
    }

    // 비즈니스 메서드 - 수정 권한 체크
    public boolean canBeEditedBy(User user) {
        return isOwnedBy(user);
    }

    // 비즈니스 메서드 - 삭제 권한 체크
    public boolean canBeDeletedBy(User user) {
        return isOwnedBy(user);
    }

}
