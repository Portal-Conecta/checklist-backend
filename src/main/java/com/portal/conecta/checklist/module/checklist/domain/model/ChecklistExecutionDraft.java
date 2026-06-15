package com.portal.conecta.checklist.module.checklist.domain.model;
 
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistAnswerResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
 
/**
 * Modelo de rascunho de execução de checklist persistido no Redis.
 *
 * <p>Armazena o estado parcial das respostas durante o preenchimento incremental.
 * Ao final do fluxo, o {@code SubmitChecklistExecutionUseCase} lê este objeto do
 * Redis, valida as respostas completas e persiste a execução definitiva no PostgreSQL.</p>
 *
 * <p>Implementa {@link Serializable} para compatibilidade com o
 * {@code JdkSerializationRedisSerializer}, mas a serialização padrão configurada
 * é JSON via Jackson ({@code GenericJackson2JsonRedisSerializer}).</p>
 *
 * @see com.portal.conecta.checklist.module.checklist.infrastructure.redis.ChecklistDraftRedisRepository
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistExecutionDraft implements Serializable {
 
    @Serial
    private static final long serialVersionUID = 1L;
 
    /** Identificador da execução no PostgreSQL — chave primária lógica do rascunho. */
    private UUID executionId;
 
    /** Usuário dono do rascunho — usado para revalidar permissão no momento do submit. */
    private UUID userId;
 
    /** Turma vinculada — usada para revalidar permissão no momento do submit. */
    private UUID classId;
 
    /**
     * Respostas parciais acumuladas.
     * Cada elemento representa a resposta mais recente para um {@code itemKey}.
     * A lista não contém duplicatas de {@code itemKey} — o merge é feito no Use Case.
     */
    @Builder.Default
    private List<ChecklistAnswerResponseDTO> answers = new ArrayList<>();
 
    /** Instante da última modificação do rascunho. Atualizado a cada save. */
    private Instant lastModifiedAt;
}