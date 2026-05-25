package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.shared.context.CurrentUserClassLink;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class HubJwtTokenProvider {

    private final SecretKey secretKey;

    public HubJwtTokenProvider(HubJwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        HubUserPrincipal principal = new HubUserPrincipal(
                requiredSubjectUuid(claims),
                requiredString(claims, "jti"),
                claimAsString(claims, "nome"),
                claimAsString(claims, "email"),
                requiredString(claims, "role"),
                requiredInteger(claims, "permissionVersion"),
                extractClassLinks(claims.get("turmas", List.class))
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                token,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.profile()))
        );
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception exception) {
            throw new BadCredentialsException("Token do Hub invalido ou expirado.", exception);
        }
    }

    private List<CurrentUserClassLink> extractClassLinks(List<?> rawClassLinks) {
        if (rawClassLinks == null) {
            return List.of();
        }

        List<CurrentUserClassLink> classLinks = new ArrayList<>();

        for (Object rawClassLink : rawClassLinks) {
            if (rawClassLink instanceof Map<?, ?> classLink) {
                classLinks.add(new CurrentUserClassLink(
                        objectAsString(classLink.get("id")),
                        objectAsString(classLink.get("relacao")),
                        objectAsString(classLink.get("papelNaTurma"))
                ));
            }
        }

        return classLinks;
    }

    private UUID requiredSubjectUuid(Claims claims) {
        String value = claims.getSubject();

        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: sub");
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new BadCredentialsException("Claim obrigatoria deve ser UUID: sub", exception);
        }
    }

    private String requiredString(Claims claims, String claimName) {
        String value = claimAsString(claims, claimName);

        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: " + claimName);
        }

        return value;
    }

    private int requiredInteger(Claims claims, String claimName) {
        Object value = claims.get(claimName);

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException exception) {
                throw new BadCredentialsException("Claim obrigatoria deve ser numerica: " + claimName, exception);
            }
        }

        throw new BadCredentialsException("Token do Hub sem claim obrigatoria: " + claimName);
    }

    private String claimAsString(Claims claims, String claimName) {
        return objectAsString(claims.get(claimName));
    }

    private String objectAsString(Object value) {
        return value == null ? null : value.toString();
    }
}
