package com.sooktin.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(description = "회원")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "email", example = "foo@sookmyung.ac.kr")
    private String email;


    @Column(nullable = false, unique = true)
    @Size(min = 2, max = 10,message = "닉네임은 2~10자 제한합니다.")
    private String nickname;


    @Schema(description = "password", example = "jamone122@3")
    @Column(nullable = false,length = 128) //비번 해시로 인한 최대 128로 저장해놓을게요
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;


    @OneToOne(mappedBy = "user")
    private CareerCard careerCard;   //my CC

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CareerCardStorage careerCardStorage;  //my CC's storage..

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
    private List<UserChatRoom> userChatRooms = new ArrayList<>();

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setCareerCardStorage(CareerCardStorage storage) {
        this.careerCardStorage = storage;
    }
}
