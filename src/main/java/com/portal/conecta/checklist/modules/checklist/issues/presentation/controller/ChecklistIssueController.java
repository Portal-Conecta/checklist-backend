package com.portal.conecta.checklist.modules.checklist.issues.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ListIssuesByExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.ResolveIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.dto.response.ChecklistIssueResponseDTO;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.mapper.ChecklistIssueMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Checklist Issues", description = "Endpoints para gerenciamento de issues/problemas de checklists")
@RestController
@RequestMapping("/api/checklist-issues")
@RequiredArgsConstructor
public class ChecklistIssueController {

    private final ListIssuesByExecutionUseCase listByExecutionUseCase;
    private final ResolveIssueUseCase resolveUseCase;
    private final ChecklistIssueMapper mapper;

    @Operation(summary = "Listar issues por execução", description = "Lista todos os problemas (issues) associados a uma execução de checklist específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação bem sucedida")
    })
    @GetMapping("/execution/{executionId}")
    public ResponseEntity<List<ChecklistIssueResponseDTO>> listByExecution(@PathVariable UUID executionId) {
        return ResponseEntity.ok(mapper.toResponseList(listByExecutionUseCase.execute(executionId)));
    }

    @Operation(summary = "Resolver issue", description = "Marca um problema (issue) específico como resolvido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Issue resolvida com sucesso")
    })
    @PatchMapping("/{issueId}/resolve")
    public ResponseEntity<ChecklistIssueResponseDTO> resolve(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(resolveUseCase.execute(issueId)));
    }
}
