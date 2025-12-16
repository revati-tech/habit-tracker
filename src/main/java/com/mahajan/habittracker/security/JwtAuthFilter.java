package com.mahajan.habittracker.security;

import com.mahajan.habittracker.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserAuthService userAuthService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT filtering for public endpoints
        return path.equals("/health") 
                || path.equals("/api/auth/login") 
                || path.equals("/api/auth/signup");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // If no header or not a Bearer token → skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // pass to next filter
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Validate token and extract email (sub)
            String email = jwtUtil.extractEmail(token);

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response); // pass to next filter
                return;
            }
            UserDetails userDetails = userAuthService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (ExpiredJwtException e) {
            // Expired tokens are expected and common - log at debug level
            log.debug("Expired JWT token for user: {}", e.getClaims().getSubject());
        } catch (JwtException e) {
            // Invalid JWT format or signature - log at warn level but without stack trace
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            // Unexpected errors - log with more detail
            log.warn("JWT authentication failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}