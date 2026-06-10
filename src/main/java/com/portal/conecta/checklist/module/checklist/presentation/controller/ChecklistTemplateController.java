package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.*;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.update.ChecklistTemplateEditRequest;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
public class ChecklistTemplateController {

    private final CreateChecklistTemplateUseCase createUseCase;
    private final ActivateChecklistTemplateUseCase activateUseCase;
    private final FindChecklistTemplateByIdUseCase findByIdUseCase;
    private final ListChecklistTemplatesUseCase listUseCase;
    private final EditChecklistTemplateUseCase editUseCase;
    private final CreateChecklistTemplateVersionUseCase createVersionUseCase;
    private final ChecklistTemplateMapper mapper;

    @PostMapping
    public ResponseEntity<ChecklistTemplateResponseDTO> createTemplate(@RequestBody @Valid ChecklistTemplateCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(createUseCase.execute(request)));
    }

    @PatchMapping("/{templateId}/activate")
    public ResponseEntity<ChecklistTemplateResponseDTO> activateTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(mapper.toResponse(activateUseCase.execute(templateId)));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> findTemplateById(@PathVariable UUID templateId) {
        return ResponseEntity.ok(mapper.toResponse(findByIdUseCase.execute(templateId)));
    }

    @GetMapping
    public ResponseEntity<List<ChecklistTemplateResponseDTO>> listTemplates() {
        return ResponseEntity.ok(mapper.toResponseList(listUseCase.execute()));
    }

    @Operation(summary = "Editar Template", description = "Atualiza parcialmente um template com status DRAFT. Campos não informados são mantidos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template atualizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para editar templates"),
            @ApiResponse(responseCode = "404", description = "Template não encontrado."),
            @ApiResponse(responseCode = "422", description = "Template não está em status DRAFT.")

    })
    @PatchMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> editTemplate(@PathVariable UUID templateId, @RequestBody @Valid ChecklistTemplateEditRequest request){
        return ResponseEntity.ok(mapper.toResponse(editUseCase.execute(templateId, request)));
    }

    @Operation(summary = "Criar nova versão", description = "Cria uma nova versão em DRAFT a partir de um template ACTIVE, preservando o histórico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nova versão criada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para versionar templates."),
            @ApiResponse(responseCode = "404", description = "Template não encontrado"),
            @ApiResponse(responseCode = "422", description = "Template não está em status ACTIVE.")
    })
    @PostMapping("/{templateId}/new-version")
    public ResponseEntity<ChecklistTemplateResponseDTO> createNewVersion(
            @PathVariable UUID templateId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(createVersionUseCase.execute(templateId)));
    }
}
