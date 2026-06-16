package com.portal.conecta.checklist.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecutionDraft;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username:default}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setUsername(redisUsername);
        redisConfig.setPassword(RedisPassword.of(redisPassword));

        JedisClientConfiguration clientConfig;

        if (sslEnabled) {
            clientConfig = JedisClientConfiguration.builder()
                    .useSsl()
                    .and()
                    .usePooling()
                    .poolConfig(jedisPoolConfig())
                    .and()
                    .build();
        } else {
            clientConfig = JedisClientConfiguration.builder()
                    .usePooling()
                    .poolConfig(jedisPoolConfig())
                    .and()
                    .build();
        }

        return new JedisConnectionFactory(redisConfig, clientConfig);
    }

    private JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }

    @Bean
    public RedisTemplate<String, ChecklistExecutionDraft> checklistDraftRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, ChecklistExecutionDraft> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        ChecklistExecutionDraftSerializer draftSerializer = new ChecklistExecutionDraftSerializer();
        template.setValueSerializer(draftSerializer);
        template.setHashValueSerializer(draftSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Serializer para {@link ChecklistExecutionDraft} usando Jackson 2.
     *
     * <p>Declarado como classe interna estática com {@code @NullMarked} para
     * satisfazer o contrato de nullability da interface {@link RedisSerializer},
     * cujo pacote é anotado com {@code @NullMarked}. Sem isso, o IntelliJ/compilador
     * emite "Not annotated method overrides method annotated with @NullMarked".</p>
     */
    @NullMarked
    static class ChecklistExecutionDraftSerializer
            implements RedisSerializer<ChecklistExecutionDraft> {

        private final ObjectMapper mapper;

        ChecklistExecutionDraftSerializer() {
            this.mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }

        @Override
        public byte[] serialize(@Nullable ChecklistExecutionDraft draft)
                throws SerializationException {
            if (draft == null) {
                return new byte[0];
            }
            try {
                return mapper.writeValueAsBytes(draft);
            } catch (Exception e) {
                throw new SerializationException(
                        "Erro ao serializar ChecklistExecutionDraft para JSON", e);
            }
        }

        @Override
        public @Nullable ChecklistExecutionDraft deserialize(byte @Nullable [] bytes)
                throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return mapper.readValue(bytes, ChecklistExecutionDraft.class);
            } catch (Exception e) {
                throw new SerializationException(
                        "Erro ao desserializar JSON para ChecklistExecutionDraft", e);
            }
        }
    }
}