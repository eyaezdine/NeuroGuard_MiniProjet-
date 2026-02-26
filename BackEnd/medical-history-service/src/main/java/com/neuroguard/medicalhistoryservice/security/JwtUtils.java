package com.neuroguard.medicalhistoryservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String secret;

    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        } catch (JWTVerificationException e) {
            log.error("JWT verification failed: {}", e.getMessage());
            throw e;
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            String userId = verifyToken(token).getClaim("id").asString();
            if (userId == null) {
                throw new RuntimeException("UserId claim missing in JWT token");
            }
            return userId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract userId from token", e);
        }
    }

    public String getRoleFromToken(String token) {
        try {
            String role = verifyToken(token).getClaim("role").asString();
            if (role == null) {
                throw new RuntimeException("Role claim missing in JWT token");
            }
            return role;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract role from token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}