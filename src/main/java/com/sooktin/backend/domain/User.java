package com.sooktin.backend.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String nickname;

    @Schema(description = "password", example = "jamone122@3")
    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    public void setIs2fa(boolean is2fa) {
        this.is2fa = is2fa;
    }

    public boolean getIs2fa() {
        return is2fa;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
