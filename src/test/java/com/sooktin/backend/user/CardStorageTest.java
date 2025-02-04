package com.sooktin.backend.user;

import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.controller.UserController;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.StorageService;
import com.sooktin.backend.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(UserController.class)
public class CardStorageTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private Validator validator;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("test@test.com")
                .password("password1!23")
                .roles(Collections.singleton(UserRole.USER))
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void should400() throws Exception {
        Mockito.when(storageService.getCardsFromStorage(anyLong())).thenThrow(new IllegalArgumentException());


    }
}
