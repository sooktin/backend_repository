package com.sooktin.backend;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = BackendApplication.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    UserRepository userRepository;



    @Test
    void boolean2fa(){
        Set<UserRole> role = Collections.singleton(UserRole.USER);

        User user = User.builder()
                .roles(role)
                .nickname("asd")
                .password("21")
                .build();

        assertThat(user.getRoles()).containsExactly(UserRole.USER);
        }
    }


