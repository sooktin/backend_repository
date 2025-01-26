package com.sooktin.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooktin.backend.controller.AuthController;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import com.sooktin.backend.dto.email.EmailCheckRequest;
import com.sooktin.backend.dto.email.EmailCheckResponse;
import com.sooktin.backend.dto.email.EmailCheckResponse;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.CustomUserDetailsService;
import com.sooktin.backend.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class EmailCheckTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;
    @MockBean
    private Validator validator;
    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private MeterRegistry meterRegistry; // Prometheus 관련 빈 Mock
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
    void shouldReturn401WhenUnauthorized() throws Exception {
        // Mock UserService to return 401 response

        EmailCheckResponse unauthorizedResponse = new EmailCheckResponse(401, "계정이 정지되었습니다. 고객센터에 문의해주세요.",false);
        Mockito.when(userService.checkEmail(anyString())).thenReturn(unauthorizedResponse);
        Mockito.when(validator.validate(any()))
                .thenReturn(Collections.emptySet()); // 유효성 검증 성공 처리
        // Prepare request
        EmailCheckRequest request = new EmailCheckRequest("unauthorized@example.com");
        String jsonRequest = objectMapper.writeValueAsString(request);

        // Perform POST request
        ResultActions result = mockMvc.perform(post("/auth/check-email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));
        String responseJson = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println("Response JSON: " + responseJson);

        // Assert response
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("계정이 정지되었습니다. 고객센터에 문의해주세요."));
    }

    @Test
    void shouldReturn500WhenInternalServerErrorOccurs() throws Exception {
        // Mock UserService to throw an exception
        Mockito.when(userService.checkEmail(anyString())).thenThrow(new RuntimeException("Internal Server Error"));

        // Prepare request
        EmailCheckRequest request = new EmailCheckRequest("error@example.com");
        String jsonRequest = objectMapper.writeValueAsString(request);

        // Perform POST request
        ResultActions result = mockMvc.perform(post("/auth/check-email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));

        String responseJson = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println("Response JSON: " + responseJson);

        // Assert response
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("내부 서버 오류입니다. 다시 접속해주세요."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}