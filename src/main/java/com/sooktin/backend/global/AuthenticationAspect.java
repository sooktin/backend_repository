package com.sooktin.backend.global;

import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.service.CustomUserDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthenticationAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object checkAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication = " + authentication);
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            ResponseDto<Object> response = new ResponseDto<>(401, "로그인이 필요합니다.", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON) // UTF-8 기본 적용
                    .body(response);
        }
        return joinPoint.proceed();
    }
}
