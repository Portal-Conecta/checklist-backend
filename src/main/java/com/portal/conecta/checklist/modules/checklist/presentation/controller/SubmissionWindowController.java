package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.window.query.ListSubmissionWindowsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.window.command.upsert.UpsertSubmissionWindowUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.window.request.SubmissionWindowRequestDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.window.response.SubmissionWindowResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.SubmissionWindowMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/submission-windows")
@RequiredArgsConstructor
@Tag(name = "Submission Windows", description = "Endpoints para gerenciamento de janelas de submissão")
public class SubmissionWindowController {

    private final UpsertSubmissionWindowUseCase upsertUseCase;
    private final ListSubmissionWindowsUseCase listUseCase;
    private final SubmissionWindowMapper mapper;

    @Operation(summary = "Listar todas as janelas de submissão", description = "Retorna todas as janelas de submissão configuradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Janelas listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão para listar as janelas", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<SubmissionWindowResponseDTO>> listAll() {
        return ResponseEntity.ok(mapper.toResponseList(listUseCase.execute()));
    }

    @Operation(summary = "Listar janelas de submissão por turma", description = "Retorna as janelas de submissão configuradas para uma turma específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Janelas listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão para acessar as janelas da turma", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    @GetMapping("/classes/{classId}")
    public ResponseEntity<List<SubmissionWindowResponseDTO>> listByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(mapper.toResponseList(listUseCase.execute(classId)));
    }

    @Operation(summary = "Criar ou atualizar janela de submissão", description = "Cria ou atualiza uma janela de submissão para uma turma e tipo de checklist específicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Janela de submissão criada ou atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão para configurar janelas de submissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    @PutMapping("/classes/{classId}/{checklistType}")
    public ResponseEntity<SubmissionWindowResponseDTO> upsert(
            @PathVariable UUID classId,
            @PathVariable ChecklistType checklistType,
            @RequestBody @Valid SubmissionWindowRequestDTO request
    ) {
        return ResponseEntity.ok(mapper.toResponse(upsertUseCase.execute(classId, checklistType, request.toCommand())));
    }
}
