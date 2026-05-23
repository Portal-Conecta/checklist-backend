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
                "id", userId.toString(),
                "nome", "Joao Silva",
                "email", "joao@exemplo.com",
                "role", "REPRESENTANTE",
                "turmas", List.of(Map.of(
                        "id", classId.toString(),
                        "relacao", "aluno",
                        "papelNaTurma", "representante"
                ))
        ), Instant.now().plusSeconds(3600));

        Authentication authentication = tokenProvider.getAuthentication(token);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(HubUserPrincipal.class);

        HubUserPrincipal principal = (HubUserPrincipal) authentication.getPrincipal();

        assertThat(principal.id()).isEqualTo(userId);
        assertThat(principal.name()).isEqualTo("Joao Silva");
        assertThat(principal.email()).isEqualTo("joao@exemplo.com");
        assertThat(principal.profile()).isEqualTo("REPRESENTANTE");
        assertThat(principal.classes()).hasSize(1);
        assertThat(principal.classes().getFirst().id()).isEqualTo(classId.toString());
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = token(Map.of(
                "id", UUID.randomUUID().toString(),
                "role", "REPRESENTANTE"
        ), Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectTokenWithoutRequiredClaims() {
        String token = token(Map.of("nome", "Joao Silva"), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    private String token(Map<String, Object> claims, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }
}
