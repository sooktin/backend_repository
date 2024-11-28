
package com.sooktin.backend.user;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void deleteUSerTest() {
        String email = "test@email.com";
        User user = User.builder()
                .email(email)
                .password("123")
                .nickname("12")
                .build();
        userRepository.save(user);

        //when
        userService.delete(email);

        //then
        assertFalse(userRepository.findByEmail(email).isPresent(),
                "사용자가 삭제되어야 함");

    }
}

