package com.portal.conecta.checklist.module.checklist.application.port.out.issue;

import java.util.Set;
import java.util.UUID;

/**
 * Porta de saida do modulo Checklist para o modulo Issues: criacao de
 * pendencias de nao-conformidade a partir de respostas de execucao.
 *
 * <p>Implementada por um adaptador dentro do modulo Issues (ver ADR-0020).</p>
 */
public interface IssueCreationPort {

    Set<String> existingItemKeysForExecution(UUID executionId);

    void createNonComplianceIssue(CreateNonComplianceIssueCommand command);
}
