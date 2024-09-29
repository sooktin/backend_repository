package com.sooktin.backend.user;

import com.sooktin.backend.dto.user.UserDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class loginTest {

    private Validator validator;

    @BeforeEach
    void setUp(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("로그인리퀘스트 validation피하나요")
    void testValidLoginReqDto(){
        UserDto.LoginRequestDto dto = new UserDto.LoginRequestDto();
        dto.setEmail("foo@ex.com");
        dto.setPassword("pa2");

        Set<ConstraintViolation<UserDto.LoginRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("로그인리퀘스트 invalid email")
    void testInvalidEmailLoginReqDto(){
        UserDto.LoginRequestDto dto = new UserDto.LoginRequestDto();
        dto.setEmail("invalid");
        dto.setPassword("pa2");

        Set<ConstraintViolation<UserDto.LoginRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("이메일 형식이 맞지 않아요",violations.iterator().next().getMessage());
    }
}
