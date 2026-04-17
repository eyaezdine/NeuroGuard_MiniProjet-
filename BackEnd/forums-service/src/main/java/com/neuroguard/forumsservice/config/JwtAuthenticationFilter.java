package com.neuroguard.forumsservice.config;


import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.validateJwtToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        DecodedJWT decodedJWT = jwtUtils.verifyToken(token);
        
        String userId = null;
        if (decodedJWT.getClaim("id") != null && !decodedJWT.getClaim("id").isNull()) {
            userId = decodedJWT.getClaim("id").asString();
        } else if (decodedJWT.getClaim("userId") != null && !decodedJWT.getClaim("userId").isNull()) {
            userId = decodedJWT.getClaim("userId").asString();
        }

        String username = null;
        if (decodedJWT.getClaim("email") != null && !decodedJWT.getClaim("email").isNull()) {
            username = decodedJWT.getClaim("email").asString();
        } else {
            username = decodedJWT.getSubject();
        }

        String role = decodedJWT.getClaim("role") != null && !decodedJWT.getClaim("role").isNull()
                      ? decodedJWT.getClaim("role").asString()
                      : "USER";

        if (username == null) {
            username = "unknown";
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        request.setAttribute("userId", userId);
        request.setAttribute("userRole", role);

        SecurityContextHolder.getContext().setAuthentication(authToken);
        chain.doFilter(request, response);
    }
}