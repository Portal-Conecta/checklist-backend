package com.portal.conecta.checklist.shared.security.token;

import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.shared.security.config.HubJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HubJwtTokenProviderTest {

    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private final HubJwtTokenProvider tokenProvider = new HubJwtTokenProvider(new HubJwtProperties(SECRET));

    @Test
    void shouldCreateAuthenticationFromValidHubToken() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        String token = token(Map.of(
                "userType", "REPRESENTATIVE",
                "classes", List.of(Map.of(
                        "classId", classId.toString(),
                        "role", "REPRESENTATIVE"
                ))
        ), userId, Instant.now().plusSeconds(3600));

        Authentication authentication = tokenProvider.getAuthentication(token);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(RequestContext.class);

        RequestContext principal = (RequestContext) authentication.getPrincipal();

        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.userType()).isEqualTo(TypeUser.REPRESENTATIVE);
        assertThat(principal.classes()).hasSize(1);
        assertThat(principal.classes().getFirst().classId()).isEqualTo(classId);
        assertThat(principal.classes().getFirst().role()).isEqualTo("REPRESENTATIVE");
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = token(Map.of(
                "userType", "STUDENT"
        ), UUID.randomUUID(), Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectTokenWithoutRequiredClaims() {
        String token = token(Map.of("userType", "STUDENT"), null, Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectTokenWithoutUserType() {
        String token = token(Map.of(), UUID.randomUUID(), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectTokenWithInvalidClassId() {
        String token = token(Map.of(
                "userType", "REPRESENTATIVE",
                "classes", List.of(Map.of(
                        "classId", "turma-1",
                        "role", "REPRESENTATIVE"
                ))
        ), UUID.randomUUID(), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectTokenWithInvalidHubRoleValues() {
        String tokenWithInvalidUserType = token(Map.of(
                "userType", "INVALID_USER_TYPE",
                "classes", List.of(Map.of(
                        "classId", UUID.randomUUID().toString(),
                        "role", "REPRESENTATIVE"
                ))
        ), UUID.randomUUID(), Instant.now().plusSeconds(3600));
        String tokenWithInvalidClassRole = token(Map.of(
                "userType", "REPRESENTATIVE",
                "classes", List.of(Map.of(
                        "classId", UUID.randomUUID().toString(),
                        "role", "INVALID_CLASS_ROLE"
                ))
        ), UUID.randomUUID(), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(tokenWithInvalidUserType))
                .isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> tokenProvider.getAuthentication(tokenWithInvalidClassRole))
                .isInstanceOf(BadCredentialsException.class);
    }

    private String token(Map<String, Object> claims, UUID subject, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiration));

        if (subject != null) {
            builder.subject(subject.toString());
        }

        return builder.signWith(key).compact();
    }
}
