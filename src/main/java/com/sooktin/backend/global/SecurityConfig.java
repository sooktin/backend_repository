package com.sooktin.backend.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/","/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf((csrf) -> csrf.disable()) //개발중에만사용
                .httpBasic(httpBasic -> {});
        return http.build();
    }*/
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/home", "/register", "/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf((csrf) -> csrf.disable())
                .formLogin((login)->login.disable())
                .httpBasic((basic)->basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder();
        return new BCryptPasswordEncoder(4);
    }
}
