package com.mahajan.habittracker.security;

import com.mahajan.habittracker.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserAuthService userAuthService;

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

        } catch (Exception e) {
            logger.warn("Invalid or expired JWT: {}", e);
        }

        filterChain.doFilter(request, response);
    }
}