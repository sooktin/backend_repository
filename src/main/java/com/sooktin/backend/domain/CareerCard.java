package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "careercards")
public class CareerCard {

    /* ERD카드 참고하여 작성한 엔티티
        아직은 미완입니다!! 수정사항 발생하면 말해주세요 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cardId")
    private Long id; // 카드ID

    @Column(length = 10, nullable = false)
    private String major; // 전공

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime created_at; // 만든날짜

    @Column(length = 255)
    private String image_url; // 이미지(s3에서 업로드)

    @Column(length = 10, nullable = false)
    private boolean student_statue; // 재학여부(휴학/재학/졸업)

    @Column(nullable = false)
    private byte grade; // 학년 (1 ,2, 3 ..etc.)

    @Column(length = 3, nullable = false)
    private String student_num; // 학번 (앞 두 자리만)

    @Column(length = 20)
    private String department; // 소속 (학과 또는 회사)

    @Column(columnDefinition = "TEXT")
    private String experience; // 경력 (길이 제한X)

    @Column(length = 100)
    private String skills; // 기술

    @Column(length = 10, nullable = false)
    private String nickname; // 닉네임 - 회원 엔티티에 있을 것!

}
