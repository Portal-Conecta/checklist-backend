package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.shared.security.solid.PortalUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    // Using a default fallback key that is 256-bit secure for HMAC-SHA256, so unit tests and local execution run without config issues.
    @Value("${jwt.secret:default-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-portal-conecta-checklist-api}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.getSubject();
        String username = claims.get("name", String.class);

        // Get profiles/roles (support both list in 'roles' or single/list in 'profile' / 'perfis')
        Set<String> profiles = new HashSet<>();
        Object rolesObj = claims.get("roles");
        if (rolesObj == null) {
            rolesObj = claims.get("perfis");
        }
        if (rolesObj instanceof List<?> list) {
            list.forEach(item -> profiles.add(item.toString()));
        } else if (rolesObj instanceof String str) {
            profiles.add(str);
        } else {
            Object profileObj = claims.get("profile");
            if (profileObj instanceof List<?> list) {
                list.forEach(item -> profiles.add(item.toString()));
            } else if (profileObj != null) {
                profiles.add(profileObj.toString());
            }
        }

        // Get representation class link
        Long turmaId = null;
        Object turmaIdObj = claims.get("turma_id");
        if (turmaIdObj == null) {
            turmaIdObj = claims.get("turmaId");
        }
        if (turmaIdObj != null) {
            try {
                turmaId = Long.valueOf(turmaIdObj.toString());
            } catch (NumberFormatException e) {
                // Keep it null or handle
            }
        }

        // Get teacher linked classes
        List<Long> linkedTurmaIds = new ArrayList<>();
        Object turmasObj = claims.get("turmas");
        if (turmasObj == null) {
            turmasObj = claims.get("turma_ids");
        }
        if (turmasObj == null) {
            turmasObj = claims.get("turmas_vinculadas");
        }
        if (turmasObj instanceof List<?> list) {
            list.forEach(item -> {
                try {
                    linkedTurmaIds.add(Long.valueOf(item.toString()));
                } catch (NumberFormatException e) {
                    // skip invalid
                }
            });
        }

        // Get organization scope
        String scope = claims.get("scope", String.class);
        if (scope == null) {
            scope = claims.get("unidade", String.class);
        }
        if (scope == null) {
            scope = claims.get("empresa", String.class);
        }

        PortalUserPrincipal principal = new PortalUserPrincipal(
                userId,
                username,
                profiles,
                turmaId,
                linkedTurmaIds,
                scope
        );

        List<SimpleGrantedAuthority> authorities = profiles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
