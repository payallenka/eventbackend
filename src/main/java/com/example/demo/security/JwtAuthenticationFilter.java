package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT authentication for WebSocket endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/ws/") || 
            "websocket".equalsIgnoreCase(request.getHeader("Upgrade"))) {
            logger.info("Skipping JWT authentication for WebSocket endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        logger.info("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        logger.info("Authorization header present: {}", authorizationHeader != null);

        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.info("JWT token extracted, length: {}", jwt.length());
            try {
                email = jwtUtil.extractEmail(jwt);
                logger.info("Email extracted from JWT: {}", email);
            } catch (Exception e) {
                logger.error("Error extracting email from JWT: {}", e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean isValidToken = jwtUtil.validateToken(jwt);
            logger.info("Token validation result: {}", isValidToken);
            
            if (isValidToken) {
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    logger.info("User found: {} with role: {}", user.getEmail(), user.getRole());
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.singletonList(authority));
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set for user: {}", email);
                } else {
                    logger.warn("User not found in database for email: {}", email);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
