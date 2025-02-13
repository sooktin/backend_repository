package com.sooktin.backend.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooktin.backend.auth.JwtAuthenticationFilter;
import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import java.util.Arrays;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    //    private final CustomUserDetailsService userDetailsService;
//    @Autowired (수정) @Autowired 제거하고 생성자로 주입
    private final StringRedisTemplate redisTemplate;

//    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
//        this.customUserDetailsService = customUserDetailsService;
//        this.jwtUtil = jwtUtil;
//        this.userDetailsService = userDetailsService;
//
//    }

    // (수정) @Autowired 제거 후 생성자 주입 방식으로 변경
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil, StringRedisTemplate redisTemplate) { // 수정
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil,redisTemplate,customUserDetailsService);
    }
    //BCryptPasswordEncoder는 passwordEncoder 인터페이스를 구현한다.
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/home", "/register", "/auth/**", 
                                   "/actuator/**", "/swagger-ui/**", "/api-docs/**").permitAll()
                    .requestMatchers("/users/**").authenticated()
                    .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), 
                               UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling -> handling
                    .authenticationEntryPoint((request, response, authException) -> {
                        ResponseDto<Object> errorResponse = new ResponseDto<>(
                            401,
                            "인증 정보가 유효하지 않습니다. 다시 로그인해주세요",
                            null
                        );
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
                    })
                )
                .build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://172.20.10.8:3000"
        )); // 프론트엔드 IP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // @Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    //     return (web) -> web.ignoring()
    //             .requestMatchers("/actuator/**", "/swagger-ui/**", "/api-docs/**");
    // }


}
