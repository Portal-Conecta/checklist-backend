package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistTemplateFacade;
import com.portal.conecta.checklist.shared.exception.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
@Tag(name = "Checklist Templates", description = "Endpoints para gerenciamento de templates de checklist")
public class ChecklistTemplateController {

    private final ChecklistTemplateFacade checklistTemplateFacade;

    @Operation(
            summary = "Criar novo template",
            description = "Cria um novo template de checklist com suas seções e itens. O template é criado no status DRAFT por padrão."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Template criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos no corpo da requisição (campos obrigatórios ausentes ou JSON malformado)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para criar templates",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de integridade — já existe um registro com os mesmos dados únicos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping
    public ResponseEntity<ChecklistTemplateResponseDTO> createTemplate(@RequestBody @Valid ChecklistTemplateCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(checklistTemplateFacade.createTemplate(request));
    }

    @Operation(
            summary = "Ativar template",
            description = "Altera o status do template para ACTIVE, tornando-o disponível para execução."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Template ativado com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato inválido para o templateId (deve ser um UUID válido)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para ativar templates",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Template não encontrado para o ID informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — template já está ativo ou não pode ser ativado no status atual",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PatchMapping("/{templateId}/activate")
    public ResponseEntity<ChecklistTemplateResponseDTO> activateTemplate(
            @Parameter(description = "UUID do template a ser ativado", required = true)
            @PathVariable UUID templateId
    ) {
        return ResponseEntity.ok(checklistTemplateFacade.activateTemplate(templateId));
    }

    @Operation(
            summary = "Buscar template por ID",
            description = "Retorna os dados completos de um template de checklist a partir do seu UUID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Template encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato inválido para o templateId (deve ser um UUID válido)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para visualizar este template",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Template não encontrado para o ID informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> findTemplateById(
            @Parameter(description = "UUID do template a ser buscado", required = true)
            @PathVariable UUID templateId
    ) {
        return ResponseEntity.ok(checklistTemplateFacade.findTemplateById(templateId));
    }

    @Operation(
            summary = "Listar todos os templates",
            description = "Retorna a lista completa de templates de checklist cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso (pode ser vazia)",
                    content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para listar templates",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<ChecklistTemplateResponseDTO>> listTemplates() {
        return ResponseEntity.ok(checklistTemplateFacade.listTemplates());
    }
}