package com.portal.conecta.checklist.shared.security.token;

import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.shared.hub.provider.user.HubUserProvider;
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

    private static final String SECRET = "dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbm90LXJlYWw=";

    private final HubJwtTokenProvider tokenProvider = new HubJwtTokenProvider(new HubJwtProperties(SECRET), id -> true);

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
    void shouldCreateAuthenticationFromChecklistHubPayload() {
        UUID userId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
        UUID teacherClassId = UUID.fromString("8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c");
        UUID studentClassId = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
        String token = token(Map.of(
                "userType", "STUDENT",
                "classes", List.of(
                        Map.of(
                                "classId", teacherClassId.toString(),
                                "role", "TEACHER"
                        ),
                        Map.of(
                                "classId", studentClassId.toString(),
                                "role", "STUDENT"
                        )
                )
        ), userId, Instant.now().plusSeconds(3600));

        Authentication authentication = tokenProvider.getAuthentication(token);

        RequestContext principal = (RequestContext) authentication.getPrincipal();
        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.userType()).isEqualTo(TypeUser.STUDENT);
        assertThat(principal.classes()).hasSize(2);
        assertThat(principal.classes().get(0).classId()).isEqualTo(teacherClassId);
        assertThat(principal.classes().get(0).role()).isEqualTo("TEACHER");
        assertThat(principal.classes().get(1).classId()).isEqualTo(studentClassId);
        assertThat(principal.classes().get(1).role()).isEqualTo("STUDENT");
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

    @Test
    void shouldValidateUserExistenceWhenHubProviderIsAvailable() {
        UUID userId = UUID.randomUUID();
        HubUserProvider userProvider = id -> id.equals(userId);
        HubJwtTokenProvider tokenProvider = new HubJwtTokenProvider(new HubJwtProperties(SECRET), userProvider);
        String token = token(Map.of("userType", "STUDENT"), userId, Instant.now().plusSeconds(3600));

        Authentication authentication = tokenProvider.getAuthentication(token);

        RequestContext principal = (RequestContext) authentication.getPrincipal();
        assertThat(principal.userId()).isEqualTo(userId);
    }

    @Test
    void shouldRejectTokenWhenUserDoesNotExistInHub() {
        HubUserProvider userProvider = id -> false;
        HubJwtTokenProvider tokenProvider = new HubJwtTokenProvider(new HubJwtProperties(SECRET), userProvider);
        String token = token(Map.of("userType", "STUDENT"), UUID.randomUUID(), Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> tokenProvider.getAuthentication(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    private String token(Map<String, Object> claims, UUID subject, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

        var builder = Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiration));

        if (subject != null) {
            builder.subject(subject.toString());
        }

        return builder.signWith(key).compact();
    }
}
