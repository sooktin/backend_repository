package com.sooktin.backend.search;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.Experience;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.global.QueryDSLConfig;
import com.sooktin.backend.repository.CareerCardRepositoryCustom;
import com.sooktin.backend.repository.CareerCardRepositoryCustomImpl;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CareerCardRepositoryCustomImpl.class, QueryDSLConfig.class})
public class CcsearchTest {
    @Qualifier("careerCardRepositoryCustomImpl")
    @Autowired
    private CareerCardRepositoryCustom careerCardRepositoryCustom;
    @Autowired
    private TestEntityManager entityManager;

    @MockBean
    private MeterRegistry meterRegistry;
    private User user1 = new User();
    private User user2 = new User();
    private CareerCard card1 = new CareerCard();
    private CareerCard card2 = new CareerCard();


    @BeforeEach
    void setUp() {

        user1 = User.builder()
                .nickname("rkWk")
                .email("21@gmail.com")
                .password("password")
                .build();
        entityManager.persist(user1);

        user2= User.builder()
                .nickname("rkWk2")
                .email("22@gmail.com")
                .password("password")
                .build();
        entityManager.persist(user2);

        card1.setUser(user1);
        card1.setMajor("데이터사이언스");
        card1.setJob("백엔드");
        card1.setDepartment("IT부서");
        card1.setGrade(3);
        card1.setStudent_status("재학");
        card1.setStudent_num("2");
        card1.setSkills(Arrays.asList("Java", "Golang", "Python"));
        List<Experience> experiences1 = new ArrayList<>();
        experiences1.add(new Experience("naver", "2022-2023"));
        card1.setExperiences(experiences1);
        entityManager.persist(card1);

        card2.setUser(user2);
        card2.setMajor("컴퓨터과학");
        card2.setJob("인프라");
        card2.setDepartment("IT부서");
        card2.setGrade(3);
        card2.setStudent_num("2");
        card2.setStudent_status("재학");
        card2.setSkills(Arrays.asList("Java", "AWS", "Python"));
        List<Experience> experiences2 = new ArrayList<>();
        experiences2.add(new Experience("MicroSoft", "2022-2023"));
        card2.setExperiences(experiences2);
        entityManager.persist(card2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("전공 검색")
    void searchByMajor() {
        String keyword = "컴퓨터";
        Pageable pageable= PageRequest.of(0,10);

        Page<CareerCard> result = careerCardRepositoryCustom.searchCareerCards(keyword, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getExperiences()).extracting("company")
                .contains("MicroSoft");
    }

}
