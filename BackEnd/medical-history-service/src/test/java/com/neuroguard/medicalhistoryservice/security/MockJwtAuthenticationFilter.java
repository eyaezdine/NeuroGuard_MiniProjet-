package com.neuroguard.medicalhistoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Mock JWT Authentication Filter for testing.
 * Accepts any Authorization header without validating the JWT signature.
 * Uses request attributes (userId, userRole) if present, otherwise uses mock values.
 */
@TestComponent
@Profile("test")
public class MockJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MockJwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String method = request.getMethod();
        String path = request.getRequestURI();
        log.debug("MockJwtAuthenticationFilter processing {} {}", method, path);

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // In test environment, we accept any Bearer token without validation
                String token = authHeader.substring(7);
                
                // Get userId and userRole from request attributes (set by test)
                Object userIdObj = request.getAttribute("userId");
                Object userRoleObj = request.getAttribute("userRole");
                
                Long userId = userIdObj instanceof Long ? (Long) userIdObj : 1L;
                String userRole = userRoleObj instanceof String ? (String) userRoleObj : "PATIENT";
                String normalizedRole = userRole.startsWith("ROLE_") ? userRole.substring(5) : userRole;
                
                log.debug("Mock JWT Filter - {} {} - UserId: {}, Role: {}", method, path, userId, normalizedRole);
                
                // Create authentication token with the role from request attributes
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + normalizedRole);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        "test-user",
                        null,
                        List.of(authority)
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Mock JWT Filter - Authentication set with authority: {}", authority);
            }
            
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Mock filter error: {}", e.getMessage(), e);
            chain.doFilter(request, response);
        }
    }
}
