package com.portal.conecta.checklist.shared.security.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link CorsProperties}.
 */
class CorsPropertiesTest {

    @Test
    void deveCriarComOrigensValidas() {
        var props = new CorsProperties(List.of("http://localhost:3000", "http://localhost:5173"));

        assertThat(props.allowedOrigins()).containsExactly("http://localhost:3000", "http://localhost:5173");
    }

    @Test
    void deveLancarExcecaoQuandoOrigensNull() {
        assertThatThrownBy(() -> new CorsProperties(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("checklist.cors.allowed-origins");
    }

    @Test
    void deveLancarExcecaoQuandoOrigensVazias() {
        assertThatThrownBy(() -> new CorsProperties(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("checklist.cors.allowed-origins");
    }
}
