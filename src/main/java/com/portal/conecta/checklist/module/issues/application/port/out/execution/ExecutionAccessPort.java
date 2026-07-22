package com.portal.conecta.checklist.module.issues.application.port.out.execution;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida do modulo Issues para o modulo Checklist: existencia e turma
 * de uma execucao, o minimo necessario para autorizar consultas de pendencias
 * (ver ADR-0020).
 *
 * <p>Implementada por um adaptador dentro do modulo Checklist.</p>
 */
public interface ExecutionAccessPort {

    Optional<UUID> findClassIdByExecutionId(UUID executionId);
}
