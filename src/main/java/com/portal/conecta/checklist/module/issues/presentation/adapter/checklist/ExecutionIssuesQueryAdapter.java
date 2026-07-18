package com.portal.conecta.checklist.module.issues.presentation.adapter.checklist;

import com.portal.conecta.checklist.module.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import com.portal.conecta.checklist.module.issues.presentation.dto.response.ChecklistIssueResponseDTO;
import com.portal.conecta.checklist.module.issues.presentation.mapper.ChecklistIssueMapper;
import com.portal.conecta.checklist.module.checklist.presentation.port.ExecutionIssuesQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador do modulo Issues para {@link ExecutionIssuesQueryPort} (dono:
 * modulo Checklist) — ver ADR-0020.
 *
 * <p>Nao reaproveita {@code ListIssuesByExecutionUseCase} de proposito: aquele
 * caso de uso faz sua propria checagem de autorizacao, que seria redundante
 * (e incorreta) aqui — quem chama esta porta (o mapper de execucao) ja rodou a
 * autorizacao da propria execucao antes de montar a resposta.</p>
 */
@Component
@RequiredArgsConstructor
public class ExecutionIssuesQueryAdapter implements ExecutionIssuesQueryPort {

    private final ChecklistIssueRepositoryPort issueRepository;
    private final ChecklistIssueMapper issueMapper;

    @Override
    public List<ChecklistIssueResponseDTO> findByExecutionId(UUID executionId) {
        return issueMapper.toResponseList(issueRepository.findAllByExecutionId(executionId));
    }
}
