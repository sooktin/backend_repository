package com.sooktin.backend.user;

<<<<<<< HEAD
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
=======
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.AuthenticationStatus;
import com.sooktin.backend.controller.UserController;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.UserService;
import jakarta.persistence.Table;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
>>>>>>> 309acbb (중간저장)
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
<<<<<<< HEAD
=======
import static org.mockito.Mockito.when;
>>>>>>> 309acbb (중간저장)

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

<<<<<<< HEAD
=======
    @MockBean
    private AuthenticationService authenticationService;
    @InjectMocks
    private UserController userController;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    public void testLoginWithNoneAccount() throws Exception {
        Long id = 1L;
        String nickname="123";
        String email ="123@example.com";
        String password="123";
        User user = User.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .password(password)
                .build();
        when(authenticationService.authenticate(email,password))
                .thenReturn(new AuthenticationResult(AuthenticationStatus.NONE_ACCOUNT,null));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("이메일이 존재하지 않아 회원가입으로 이동합니다."));
    }
>>>>>>> 309acbb (중간저장)

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
