package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
public class Community {
    //순수 데이터
    //게시글 조건) Primary Key인 노트ID를 추가하세요. 노트ID는 Long값입니다.
    //내용은 300자 이하이며 not null 제한/좋아요는 null값 허용/생성일시와 수정일시는 timestamp형식
    //게시글 생성/수정/삭제/갖고오기(read)하기.
    //데이터의 속성은 내용 좋아요 생성일시,수정일시가 있음 우선 내용,좋아요만!(가능하다면 이미지도)


    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 키 값 자동으로 생성
    private Long post_id; // 노트id
    @Column(length = 25, nullable = false) private String title; // 제목
    @Column(length = 300, nullable = false) private String content; // 내용
    private Integer likes; // 좋아요 - 임시생성(나중에 BoardLike 기능으로 뺄 예정)
    @CreationTimestamp @NotNull private LocalDateTime updated_at; // 생성일시
    @UpdateTimestamp @NotNull private LocalDateTime modified_at; // 수정일시

}
