package com.sooktin.backend.auth;

import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String token = getJwtFromRequest(request);
            // refresh-token endpoint는 만료된 토큰도 허용
            boolean isRefreshRequest = request.getRequestURI().equals("/auth/refresh-token");
            String path = request.getRequestURI();
            if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/api-docs")) {
                filterChain.doFilter(request, response);
                return;
            }
            if (StringUtils.hasText(token)) {
                Jws<Claims> claims = jwtUtil.parserClaims(token);
                String email = claims.getPayload().getSubject();

                String storedToken = redisTemplate.opsForValue().get("REFRESH_"+email);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {

                    CustomUserDetails userDetails =
                            (CustomUserDetails) userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request,response);
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid jwt token");
        }
    }

    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.contains("swagger-ui") ||  // 추가
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/actuator") ||
                 path.startsWith("/app") ||
                path.startsWith("/topic") || path.startsWith("/queue") ||
                path.startsWith("/ws") || path.startsWith("/ws/")||
                path.equals("/ws/chat") ||
                path.equals("/error") ||
                path.matches(".*/websocket") ||
                path.matches(".*/info") ||
                path.matches(".*/sockjs-.*");;

        log.info("Request path: {}, Should skip filter: {}", path, shouldSkip);
        return shouldSkip;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
