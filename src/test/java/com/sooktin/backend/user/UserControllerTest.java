package com.sooktin.backend.user;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.UserService;
import jakarta.persistence.Table;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;


    @Transactional
    @Rollback
    @Test
    public void testAllExists() throws Exception {
        User user1=User.builder()
                .email("you@sookmyung.ac.kr")
                .nickname("sk")
                .password("12321")
                .roles(Collections.singleton(UserRole.USER))
                .is2fa(true)
                .build();

        User user2=User.builder()
                .email("u@sookmyung.ac.kr")
                .nickname("ka")
                .password("123")
                .roles(Collections.singleton(UserRole.USER))
                .is2fa(true)
                .build();

        userRepository.saveAll(Arrays.asList(user1,user2));

        ResultActions resultActions = mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.ALL));

        resultActions.andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].nickname", Matchers.is("sk")))
                .andExpect(jsonPath("$[1].nickname", Matchers.is("ka")));

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting("nickname").containsExactlyInAnyOrder("sk","ka");
    }
}
