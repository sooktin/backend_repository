package com.sooktin.backend.dto.user;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class UserGetResponse {
    private Long id;
    private String email;
    private String nickname;
    private Set<UserRole> roles;

    public static UserGetResponse from(User user) {
        return new UserGetResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRoles()
        );
    }
}
