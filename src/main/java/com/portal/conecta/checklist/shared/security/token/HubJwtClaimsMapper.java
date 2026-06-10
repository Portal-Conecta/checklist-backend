package com.portal.conecta.checklist.shared.security.token;

import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converte as claims brutas do JWT do Hub para o contexto de autorizacao da Checklist API.
 */
@Component
public class HubJwtClaimsMapper {

    public RequestContext toRequestContext(Claims claims) {
        validateRegisteredClaims(claims);

        return new RequestContext(
                requiredUuid(claims.getSubject(), "sub"),
                requiredEnum(claims.get("userType"), "userType", TypeUser.class),
                extractClassLinks(claims.get("classes"))
        );
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
                    requiredEnum(classLink.get("role"), "classes[].role", ClassRole.class)
            ));
        }

        return classLinks;
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

    private <T extends Enum<T>> T requiredEnum(Object rawValue, String claimName, Class<T> enumType) {
        String value = objectAsString(rawValue);

        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Token do Hub sem claim obrigatoria: " + claimName);
        }

        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException exception) {
            throw new BadCredentialsException("Claim obrigatoria possui valor invalido: " + claimName, exception);
        }
    }

    private String objectAsString(Object value) {
        return value == null ? null : value.toString();
    }
}
