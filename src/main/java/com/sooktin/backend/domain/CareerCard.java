package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "careerCards")
public class CareerCard {

    /* ERD카드 참고하여 작성한 엔티티
        아직은 미완입니다!! 수정사항 발생하면 말해주세요 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cardId")
    private Long id; // 카드ID

    @OneToOne(fetch = FetchType.LAZY) // 회원ID 외래키 - 일대일
    @JoinColumn(name = "user_id", nullable = false) // 외래키 선언
    @JsonIgnore //순환 참조 방지
    private User user; // 회원 정보 (User 엔티티와 연결)

  /*  @ManyToOne(fetch = FetchType.LAZY) // 보관Id 외래키 - 다대일
    @JoinColumn(name = "storage_id", nullable = false) // 외래키 선언
    private CareerCard_store careerCardStore; // CareerCard_store 엔티티와 연결*/

    @Column(length = 20, nullable = false)
    private String major; // 전공

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime created_at; // 생성일시

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modified_at; // 수정일시

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_card_images", joinColumns = @JoinColumn(name = "career_card_id"))
    @Column(name = "image_url", length = 255)
    private List<String> imageUrls = new ArrayList<>(); // 이미지

    @Column(length = 10, nullable = false)
    private String student_status; // 재학여부(휴학/재학/졸업)

    @Column(nullable = false)
    private Integer grade; // 학년 (1 ,2, 3 ..etc.)

    @Column(length = 3, nullable = false)
    private String student_num; // 학번 (앞 두 자리만)

    @Column(length = 30)
    private String department; // 소속

    @Column(length = 30)
    private String job; // 직업 (최대 20자)

    //N+1문제가능성도... @BatchSize(10)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_card_experiences", joinColumns = @JoinColumn(name = "career_card_id"))
    private List<Experience> experiences = new ArrayList<>(); // 경력 (회사 + 기간)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_card_skills", joinColumns = @JoinColumn(name = "career_card_id"))
    @Column(name = "skill", length = 100)
    private List<String> skills = new ArrayList<>(); // 기술

    // 나중에 user 수가 증가하면  @ElementCollection -> 별도 엔티티 분리로 리팩토링 해야합니다!

    @OneToMany(mappedBy = "careerCard", cascade = CascadeType.ALL)
    private List<StorageCardMapping> storageMappings = new ArrayList<>();

}
