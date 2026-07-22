package com.portal.conecta.checklist.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Habilita o cache de aplicacao (Spring Cache) com Caffeine.
 *
 * <p>Usado pelo dashboard composto ({@code checklist-dashboard}): entradas
 * expiram {@value #TTL_SECONDS}s apos a escrita, entao as agregacoes pesadas sao
 * recalculadas no maximo uma vez por janela, independentemente de quantos
 * usuarios/pollings baterem no endpoint. TTL curto mantem o dado "fresco o
 * suficiente" para stats que mudam devagar, sem precisar de websocket.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final int TTL_SECONDS = 5;
    private static final int MAX_ENTRIES = 500;

    public static final String DASHBOARD_CACHE = "checklist-dashboard";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(DASHBOARD_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(TTL_SECONDS))
                .maximumSize(MAX_ENTRIES));
        return cacheManager;
    }
}
