package com.portal.conecta.checklist.modules.checklist.issues.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ListIssuesByExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.resolved.ResolveIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.start.StartIssueProgressUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.validate.ValidateIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.reopen.ReopenIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.start.RestartProgressIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.cancel.CancelIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.dto.response.ChecklistIssueResponseDTO;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.mapper.ChecklistIssueMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
    private final StartIssueProgressUseCase startUseCase;
    private final ValidateIssueUseCase validateUseCase;
    private final ReopenIssueUseCase reopenUseCase;
    private final RestartProgressIssueUseCase restartProgressUseCase;
    private final CancelIssueUseCase cancelUseCase;
    private final ChecklistIssueMapper mapper;

    @Operation(summary = "Listar issues por execução", description = "Lista todos os problemas (issues) associados a uma execução de checklist específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esta execução", content = @Content),
            @ApiResponse(responseCode = "404", description = "Execução de checklist não encontrada", content = @Content)
    })
    @GetMapping("/execution/{executionId}")
    public ResponseEntity<List<ChecklistIssueResponseDTO>> listByExecution(@PathVariable UUID executionId) {
        return ResponseEntity.ok(mapper.toResponseList(listByExecutionUseCase.execute(executionId)));
    }

    @PatchMapping("/{issueId}/start")
    public ResponseEntity<ChecklistIssueResponseDTO> start(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(startUseCase.execute(issueId)));
    }

    @Operation(summary = "Resolver issue", description = "Marca um problema (issue) específico como resolvido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Issue resolvida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Issue não pode ser resolvida (ex: já resolvida)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para resolver esta issue", content = @Content),
            @ApiResponse(responseCode = "404", description = "Issue não encontrada", content = @Content)
    })
    @PatchMapping("/{issueId}/resolve")
    public ResponseEntity<ChecklistIssueResponseDTO> resolve(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(resolveUseCase.execute(issueId)));
    }

    @PatchMapping("/{issueId}/validate")
    public ResponseEntity<ChecklistIssueResponseDTO> validate(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(validateUseCase.execute(issueId)));
    }

    @PatchMapping("/{issueId}/reopen")
    public ResponseEntity<ChecklistIssueResponseDTO> reopen(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(reopenUseCase.execute(issueId)));
    }

    @PatchMapping("/{issueId}/restart-progress")
    public ResponseEntity<ChecklistIssueResponseDTO> restartProgress(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(restartProgressUseCase.execute(issueId)));
    }

    @PatchMapping("/{issueId}/cancel")
    public ResponseEntity<ChecklistIssueResponseDTO> cancel(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(cancelUseCase.execute(issueId)));
    }
}

