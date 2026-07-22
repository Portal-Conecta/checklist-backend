package com.portal.conecta.checklist.module.checklist.application.port.out.issue;

import java.util.Set;
import java.util.UUID;

/**
 * Porta de saida do modulo Checklist para o modulo Issues: criacao de
 * pendencias de nao-conformidade a partir de respostas de execucao, e consulta
 * de quais itens estao com pendencia em aberto.
 *
 * <p>Implementada por um adaptador dentro do modulo Issues (ver ADR-0020).</p>
 */
public interface IssueCreationPort {

    Set<String> existingItemKeysForExecution(UUID executionId);

    void createNonComplianceIssue(CreateNonComplianceIssueCommand command);

    /**
     * Itens da execucao cuja pendencia ainda nao foi validada (qualquer status
     * exceto {@code VALIDATED}/{@code CANCELED}). O modulo Checklist usa isso pra
     * travar a edicao do valor de conformidade desses itens ate a validacao.
     */
    Set<String> lockedItemKeys(UUID executionId);
}
