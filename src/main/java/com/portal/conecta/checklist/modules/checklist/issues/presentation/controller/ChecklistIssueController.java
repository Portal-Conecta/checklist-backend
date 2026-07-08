package com.portal.conecta.checklist.modules.checklist.issues.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ListIssuesByExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.ResolveIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.StartIssueProgressUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.ValidateIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.ReopenIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.RestartProgressIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.CancelIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.dto.response.ChecklistIssueResponseDTO;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.mapper.ChecklistIssueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/execution/{executionId}")
    public ResponseEntity<List<ChecklistIssueResponseDTO>> listByExecution(@PathVariable UUID executionId) {
        return ResponseEntity.ok(mapper.toResponseList(listByExecutionUseCase.execute(executionId)));
    }

    @PatchMapping("/{issueId}/start")
    public ResponseEntity<ChecklistIssueResponseDTO> start(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(startUseCase.execute(issueId)));
    }

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

