package com.portal.conecta.checklist.module.checklist.presentation.port;

import com.portal.conecta.checklist.module.issues.presentation.dto.response.ChecklistIssueResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Porta de saida do modulo Checklist para o modulo Issues: pendencias de uma
 * execucao, ja no formato de resposta HTTP, para compor
 * {@code ChecklistExecutionResponseDTO} (ver ADR-0020).
 *
 * <p>Vive em {@code presentation}, nao em {@code application}, porque retorna
 * um DTO de apresentacao do modulo Issues — colocar essa porta em
 * {@code application} violaria a regra de arquitetura "application nao
 * depende de presentation".</p>
 */
public interface ExecutionIssuesQueryPort {

    List<ChecklistIssueResponseDTO> findByExecutionId(UUID executionId);
}
