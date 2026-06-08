package com.portal.conecta.checklist.module.issues.presentation.controller;

import com.portal.conecta.checklist.module.issues.application.usecase.ListIssuesByExecutionUseCase;
import com.portal.conecta.checklist.module.issues.application.usecase.ResolveIssueUseCase;
import com.portal.conecta.checklist.module.issues.presentation.dto.response.ChecklistIssueResponseDTO;
import com.portal.conecta.checklist.module.issues.presentation.mapper.ChecklistIssueMapper;
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
    private final ChecklistIssueMapper mapper;

    @GetMapping("/execution/{executionId}")
    public ResponseEntity<List<ChecklistIssueResponseDTO>> listByExecution(@PathVariable UUID executionId) {
        return ResponseEntity.ok(mapper.toResponseList(listByExecutionUseCase.execute(executionId)));
    }

    @PatchMapping("/{issueId}/resolve")
    public ResponseEntity<ChecklistIssueResponseDTO> resolve(@PathVariable UUID issueId) {
        return ResponseEntity.ok(mapper.toResponse(resolveUseCase.execute(issueId)));
    }
}
