package com.portal.conecta.checklist.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HubJwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256";

    private final HubJwtTokenProvider tokenProvider = new HubJwtTokenProvider(new HubJwtProperties(SECRET));

    @Test
    void shouldCreateAuthenticationFromValidHubToken() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        String token = token(Map.of(
                "jti", "abc-xyz-789",
                "sub", userId.toString(),
                "role", "aluno",
                "permissionVersion", 4,
                "turmas", List.of(Map.of(
                        "id", classId.toString(),
                        "papelNaTurma", "representante"
                ))
        ), Instant.now().plusSeconds(3600));

        Authentication authentication = tokenProvider.getAuthentication(token);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(HubUserPrincipal.class);

        HubUserPrincipal principal = (HubUserPrincipal) authentication.getPrincipal();

        assertThat(principal.id()).isEqualTo(userId);
        assertThat(principal.tokenId()).isEqualTo("abc-xyz-789");
        assertThat(principal.name()).isNull();
        assertThat(principal.email()).isNull();
        assertThat(principal.profile()).isEqualTo("aluno");
        assertThat(principal.permissionVersion()).isEqualTo(4);
        assertThat(principal.classes()).hasSize(1);
        assertThat(principal.classes().getFirst().id()).isEqualTo(classId.toString());
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = token(Map.of(
                "sub", UUID.randomUUID().toString(),
                "jti", "expired-token",
                "role", "aluno",
                "permissionVersion", 1
        ), Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectTokenWithoutRequiredClaims() {
        String token = token(Map.of("role", "aluno"), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectTokenWithoutPermissionVersion() {
        String token = token(Map.of(
                "jti", "token-without-version",
                "sub", UUID.randomUUID().toString(),
                "role", "aluno"
        ), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    private String token(Map<String, Object> claims, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Object subject = claims.get("sub");

        var builder = Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiration));

        if (subject != null) {
            builder.subject(subject.toString());
        }

        return builder
                .signWith(key)
                .compact();
    }
}
