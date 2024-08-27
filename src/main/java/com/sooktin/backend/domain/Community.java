package com.sooktin.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Community {
    //순수 데이터
    //게시글 조건) Primary Key인 노트ID를 추가하세요. 노트ID는 Long값입니다.
    //내용은 300자 이하이며 not null 제한/좋아요는 null값 허용/생성일시와 수정일시는 timestamp형식
    //게시글 생성/수정/삭제/갖고오기(read)하기.
    //데이터의 속성은 내용 좋아요 생성일시,수정일시가 있음 우선 내용,좋아요만!(가능하다면 이미지도)
    @Id

    @NotEmpty
    private String title;
    @NotEmpty
    private String content;

    private
    /*
    제목, 내용, 좋아요, 생성일시, 수정일시
    */
    //좋아요는 BoardLike 기능으로 빼기
}
