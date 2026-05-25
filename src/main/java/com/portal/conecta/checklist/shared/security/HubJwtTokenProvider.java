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
                requiredUuid(claims, "id"),
                requiredString(claims, "nome"),
                requiredString(claims, "email"),
                requiredString(claims, "role"),
                extractClassLinks(claims.get("turmas"))
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

    private List<CurrentUserClassLink> extractClassLinks(Object rawClassLinks) {
        if (rawClassLinks == null) {
            return List.of();
        }

        if (!(rawClassLinks instanceof List<?> rawClassLinkList)) {
            throw new BadCredentialsException("Claim turmas deve ser uma lista.");
        }

        List<CurrentUserClassLink> classLinks = new ArrayList<>();

        for (Object rawClassLink : rawClassLinkList) {
            if (!(rawClassLink instanceof Map<?, ?> classLink)) {
                throw new BadCredentialsException("Itens de turmas devem ser objetos.");
            }

            classLinks.add(new CurrentUserClassLink(
                    requiredUuid(classLink.get("id"), "turmas[].id"),
                    objectAsString(classLink.get("relacao")),
                    objectAsString(classLink.get("papelNaTurma"))
            ));
        }

        return classLinks;
    }

    private UUID requiredUuid(Claims claims, String claimName) {
        return requiredUuid(claims.get(claimName), claimName);
    }

    private UUID requiredUuid(Object rawValue, String claimName) {
        String value = objectAsString(rawValue);

        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: " + claimName);
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new BadCredentialsException("Claim obrigatoria deve ser UUID: " + claimName, exception);
        }
    }

    private String requiredString(Claims claims, String claimName) {
        String value = claimAsString(claims, claimName);

        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: " + claimName);
        }

        return value;
    }

    private String claimAsString(Claims claims, String claimName) {
        return objectAsString(claims.get(claimName));
    }

    private String objectAsString(Object value) {
        return value == null ? null : value.toString();
    }
}
