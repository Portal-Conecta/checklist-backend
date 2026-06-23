package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.window.query.ListSubmissionWindowsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.window.command.upsert.UpsertSubmissionWindowUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.window.request.SubmissionWindowRequestDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.window.response.SubmissionWindowResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.SubmissionWindowMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/submission-windows")
@RequiredArgsConstructor
public class SubmissionWindowController {

    private final UpsertSubmissionWindowUseCase upsertUseCase;
    private final ListSubmissionWindowsUseCase listUseCase;
    private final SubmissionWindowMapper mapper;

    @GetMapping
    public ResponseEntity<List<SubmissionWindowResponseDTO>> listAll() {
        return ResponseEntity.ok(mapper.toResponseList(listUseCase.execute()));
    }

    @GetMapping("/classes/{classId}")
    public ResponseEntity<List<SubmissionWindowResponseDTO>> listByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(mapper.toResponseList(listUseCase.execute(classId)));
    }

    @PutMapping("/classes/{classId}/{checklistType}")
    public ResponseEntity<SubmissionWindowResponseDTO> upsert(
            @PathVariable UUID classId,
            @PathVariable ChecklistType checklistType,
            @RequestBody @Valid SubmissionWindowRequestDTO request
    ) {
        return ResponseEntity.ok(mapper.toResponse(upsertUseCase.execute(classId, checklistType, request.toCommand())));
    }
}
