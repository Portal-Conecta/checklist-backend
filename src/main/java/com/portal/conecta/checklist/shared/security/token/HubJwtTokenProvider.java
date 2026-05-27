package com.portal.conecta.checklist.shared.security.token;

import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.shared.hub.provider.user.HubUserProvider;
import com.portal.conecta.checklist.shared.security.config.HubJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class HubJwtTokenProvider {

    private static final Set<String> HUB_CLASS_ROLES = Set.of(
            "STUDENT",
            "TEACHER",
            "REPRESENTATIVE"
    );

    private final SecretKey secretKey;
    private final HubUserProvider hubUserProvider;

    public HubJwtTokenProvider(HubJwtProperties properties, HubUserProvider hubUserProvider) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.hubUserProvider = hubUserProvider;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        validateRegisteredClaims(claims);

        UUID userId = requiredSubjectUuid(claims);
        TypeUser userType = requiredTypeUser(claims.get("userType"));
        List<ContextClass> classLinks = extractClassLinks(claims.get("classes"));
        validateUserExists(userId);

        RequestContext principal = new RequestContext(
                userId,
                userType,
                classLinks
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                token,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.userType().name()))
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

    private void validateRegisteredClaims(Claims claims) {
        requiredUuid(claims.getId(), "jti");

        if (claims.getIssuedAt() == null) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: iat");
        }

        if (claims.getExpiration() == null) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: exp");
        }
    }

    private void validateUserExists(UUID userId) {
        if (!hubUserProvider.existsById(userId)) {
            throw new BadCredentialsException("Usuario do token nao encontrado no Hub.");
        }
    }

    private List<ContextClass> extractClassLinks(Object rawClassLinks) {
        if (rawClassLinks == null) {
            return List.of();
        }

        if (!(rawClassLinks instanceof List<?> rawClassLinkList)) {
            throw new BadCredentialsException("Claim classes deve ser uma lista.");
        }

        List<ContextClass> classLinks = new ArrayList<>();

        for (Object rawClassLink : rawClassLinkList) {
            if (!(rawClassLink instanceof Map<?, ?> classLink)) {
                throw new BadCredentialsException("Itens de classes devem ser objetos.");
            }

            classLinks.add(new ContextClass(
                    requiredUuid(classLink.get("classId"), "classes[].classId"),
                    requiredAllowedString(classLink.get("role"), "classes[].role", HUB_CLASS_ROLES)
            ));
        }

        return classLinks;
    }

    private UUID requiredSubjectUuid(Claims claims) {
        return requiredUuid(claims.getSubject(), "sub");
    }

    private TypeUser requiredTypeUser(Object rawValue) {
        String value = requiredString(rawValue, "userType");

        try {
            return TypeUser.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new BadCredentialsException("Claim obrigatoria possui valor invalido: userType");
        }
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

    private String requiredAllowedString(Object rawValue, String claimName, Set<String> allowedValues) {
        String value = requiredString(rawValue, claimName);

        if (!allowedValues.contains(value)) {
            throw new BadCredentialsException("Claim obrigatoria possui valor invalido: " + claimName);
        }

        return value;
    }

    private String requiredString(Object rawValue, String claimName) {
        String value = objectAsString(rawValue);

        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: " + claimName);
        }

        return value;
    }

    private String objectAsString(Object value) {
        return value == null ? null : value.toString();
    }
}
