package com.neuroguard.medicalhistoryservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Decode JWT claims without verification (for Keycloak tokens)
     * In production, verify the signature using Keycloak's public key
     */
    public Map<String, Object> getAllClaimsFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid JWT format");
            }

            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            return claims;
        } catch (Exception e) {
            log.error("Failed to decode JWT claims: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            Map<String, Object> claims = getAllClaimsFromToken(token);

            // Try multiple possible claim names for user ID
            String userId = null;
            if (claims.containsKey("sub")) {
                userId = (String) claims.get("sub");
            } else if (claims.containsKey("id")) {
                userId = (String) claims.get("id");
            } else if (claims.containsKey("userId")) {
                userId = (String) claims.get("userId");
            } else if (claims.containsKey("preferred_username")) {
                userId = (String) claims.get("preferred_username");
            }

            if (userId == null || userId.isEmpty()) {
                log.warn("UserId not found in token claims. Available claims: {}", claims.keySet());
                userId = claims.getOrDefault("sub", "unknown").toString();
            }
            return userId;
        } catch (Exception e) {
            log.error("Failed to extract userId from token: {}", e.getMessage());
            throw new RuntimeException("Failed to extract userId from token", e);
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Map<String, Object> claims = getAllClaimsFromToken(token);

            // Try multiple possible claim names for role
            String role = null;
            if (claims.containsKey("role")) {
                role = (String) claims.get("role");
            } else if (claims.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                role = roles != null && !roles.isEmpty() ? roles.get(0) : "USER";
            } else if (claims.containsKey("realm_access")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                role = roles != null && !roles.isEmpty() ? roles.get(0) : "USER";
            } else if (claims.containsKey("resource_access")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
                if (resourceAccess != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("neuroguard-client");
                    if (clientAccess != null) {
                        @SuppressWarnings("unchecked")
                        List<String> roles = (List<String>) clientAccess.get("roles");
                        role = roles != null && !roles.isEmpty() ? roles.get(0) : "USER";
                    }
                }
            }

            if (role == null || role.isEmpty()) {
                log.warn("Role not found in token claims. Using default. Available claims: {}", claims.keySet());
                role = "USER";
            }
            return role;
        } catch (Exception e) {
            log.warn("Failed to extract role from token, using default USER: {}", e.getMessage());
            return "USER";
        }
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}

