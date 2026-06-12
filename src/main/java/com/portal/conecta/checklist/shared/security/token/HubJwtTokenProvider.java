package com.portal.conecta.checklist.shared.security.token;

import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import com.portal.conecta.checklist.shared.integration.hub.adapter.me.HubMeProvider;
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
import java.util.List;
import java.util.UUID;

/**
 * Orquestra a autenticacao por access token emitido pelo Hub.
 *
 * <p>Esta classe nao interpreta HTTP e nao aplica regra de negocio do modulo. Ela apenas
 * valida o JWT, converte claims para o contexto da requisicao, confirma se o usuario
 * existe no Hub e cria o objeto de autenticacao do Spring.</p>
 */
@Component
public class HubJwtTokenProvider {

    private final SecretKey secretKey;
    private final HubJwtClaimsMapper claimsMapper;
    private final HubMeProvider hubMeProvider;

    public HubJwtTokenProvider(
            HubJwtProperties properties,
            HubJwtClaimsMapper claimsMapper,
            HubMeProvider hubMeProvider
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.claimsMapper = claimsMapper;
        this.hubMeProvider = hubMeProvider;
    }

    /**
     * Valida o JWT e retorna uma autenticacao pronta para o SecurityContext.
     *
     * @param token access token sem o prefixo {@code Bearer}
     * @return autenticacao do Spring com RequestContext como principal
     * @throws BadCredentialsException se o token for invalido ou se o usuario nao existir no Hub
     * @throws HubIntegrationException se o Hub estiver indisponivel ao validar o usuario
     */
    public Authentication getAuthentication(String token) {
        RequestContext principal = claimsMapper.toRequestContext(parseClaims(token));
        validateAuthenticatedUser(principal.userId());

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

    private void validateAuthenticatedUser(UUID userId) {
        if (!hubMeProvider.existsAuthenticatedUser(userId)) {
            throw new BadCredentialsException("Usuario do token nao encontrado no Hub.");
        }
    }
}
