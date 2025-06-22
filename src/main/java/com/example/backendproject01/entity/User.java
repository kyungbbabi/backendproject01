package com.example.backendproject01.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder // 객체를 생성할 때 체이닝 방식(각 메서드가 객체 자신(this)을 반환해서 다음 메서드를 바로 호출)으로 값을 설정하는 패턴
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId;
    private String password;
    private String name;

    // mappedBy = "author", User가 주인이 아니라 Memo의 author 필드가 연관관계의 주인
    // cascade = CascadeType.ALL, 부모 엔티티의 상태 변화가 자식 엔티티에도 전파됨
    // orphanRemoval = true, 부모와 연결이 끊긴 자식 엔티티를 자동으로 삭제
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Memo> memos = new ArrayList<>();

    // 비즈니스 메서드 - 메모 추가
    public void addMemo(Memo memo) {
        memos.add(memo);
    }

}
