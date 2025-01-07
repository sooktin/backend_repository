package com.sooktin.backend.dto.user;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameResponse extends ResponseDto<UserDto> {
    public NicknameResponse(int statusCode, String message, User user) {
        super(statusCode, message, new UserDto(user.getId(),user.getEmail(),user.getNickname()));
    }

}
