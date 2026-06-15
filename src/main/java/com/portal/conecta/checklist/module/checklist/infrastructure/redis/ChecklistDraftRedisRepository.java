package com.portal.conecta.checklist.module.checklist.infrastructure.redis;
 
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecutionDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
 
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
 
/**
 * Repositório responsável pela persistência de rascunhos de execução de checklist no Redis.
 *
 * <h3>Estratégia de chave</h3>
 * <pre>
 *   checklist:draft:{executionId}
 * </pre>
 * <p>O prefixo {@code checklist:draft:} agrupa todas as chaves deste domínio,
 * facilitando inspeção via {@code SCAN 0 MATCH checklist:draft:*} e evitando
 * colisões com outros módulos que possam usar o mesmo Redis.</p>
 *
 * <h3>TTL</h3>
 * <p>Configurável via {@code checklist.draft.ttl-hours} (padrão: 4h).
 * O TTL é renovado a cada salvamento — o contador reinicia sempre que o usuário
 * interagir com o rascunho.</p>
 */
@Repository
@RequiredArgsConstructor
public class ChecklistDraftRedisRepository {
 
    private static final String KEY_PREFIX = "checklist:draft:";
 
    private final RedisTemplate<String, ChecklistExecutionDraft> redisTemplate;
 
    @Value("${checklist.draft.ttl-hours:4}")
    private long ttlHours;
 
    /**
     * Salva ou atualiza o rascunho no Redis, renovando o TTL.
     *
     * @param draft rascunho a ser persistido.
     */
    public void save(ChecklistExecutionDraft draft) {
        String key = buildKey(draft.getExecutionId());
        redisTemplate.opsForValue().set(key, draft, Duration.ofHours(ttlHours));
    }
 
    /**
     * Busca o rascunho de uma execução pelo seu ID.
     *
     * @param executionId identificador da execução no PostgreSQL.
     * @return {@link Optional} com o rascunho, ou vazio se não existir / tiver expirado.
     */
    public Optional<ChecklistExecutionDraft> findByExecutionId(UUID executionId) {
        ChecklistExecutionDraft draft = redisTemplate.opsForValue().get(buildKey(executionId));
        return Optional.ofNullable(draft);
    }
 
    /**
     * Remove o rascunho do Redis.
     * Deve ser chamado após o submit bem-sucedido para liberar memória imediatamente,
     * sem aguardar o TTL natural.
     *
     * @param executionId identificador da execução cujo rascunho será removido.
     */
    public void deleteByExecutionId(UUID executionId) {
        redisTemplate.delete(buildKey(executionId));
    }
 
    /**
     * Verifica se existe um rascunho ativo para a execução informada.
     *
     * @param executionId identificador da execução.
     * @return {@code true} se a chave existir e não tiver expirado.
     */
    public boolean existsByExecutionId(UUID executionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(executionId)));
    }
 
    // -------------------------------------------------------------------------
 
    private String buildKey(UUID executionId) {
        return KEY_PREFIX + executionId;
    }
}