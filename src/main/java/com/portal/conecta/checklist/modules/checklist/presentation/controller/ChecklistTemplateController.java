package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.CreateChecklistTemplateVersionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.EditChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.FindChecklistTemplateByIdUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.request.ChecklistTemplateEditRequest;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.ChecklistTemplateMapper;
import com.portal.conecta.checklist.shared.exception.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Checklist Templates", description = "Endpoints para gerenciamento de templates de checklist")
public class ChecklistTemplateController {

        private final CreateChecklistTemplateUseCase createUseCase;
        private final ActivateChecklistTemplateUseCase activateUseCase;
        private final FindChecklistTemplateByIdUseCase findByIdUseCase;
        private final ListChecklistTemplatesUseCase listUseCase;
        private final EditChecklistTemplateUseCase editUseCase;
        private final CreateChecklistTemplateVersionUseCase createVersionUseCase;
        private final ChecklistTemplateMapper mapper;

        @Operation(summary = "Criar novo template", description = "Cria um novo template de checklist com suas seções e itens. O template é criado no status DRAFT por padrão.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Template criado com sucesso", content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição (campos obrigatórios ausentes ou JSON malformado)", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado — token JWT ausente ou inválido", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para criar templates", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "409", description = "Conflito de integridade — já existe um registro com os mesmos dados únicos", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "500", description = "Erro interno inesperado no servidor", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
        })
        @PostMapping
        public ResponseEntity<ChecklistTemplateResponseDTO> createTemplate(
                        @RequestBody @Valid ChecklistTemplateCreateRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(mapper.toResponseWithEnrichment(createUseCase.execute(request.toCommand())));
        }

        @Operation(summary = "Ativar template", description = "Altera o status do template para ACTIVE, tornando-o disponível para execução.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Template ativado com sucesso", content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Formato inválido para o templateId (deve ser um UUID válido)", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado — token JWT ausente ou inválido", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para ativar templates", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Template não encontrado para o ID informado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "409", description = "Conflito de estado — template já está ativo ou não pode ser ativado no status atual", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "500", description = "Erro interno inesperado no servidor", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
        })
        @PatchMapping("/{templateId}/activate")
        public ResponseEntity<ChecklistTemplateResponseDTO> activateTemplate(@PathVariable UUID templateId) {
                return ResponseEntity.ok(mapper.toResponseWithEnrichment(activateUseCase.execute(templateId)));
        }

        @Operation(summary = "Buscar template por ID", description = "Retorna os dados completos de um template de checklist a partir do seu UUID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Template encontrado com sucesso", content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Formato inválido para o templateId (deve ser um UUID válido)", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado — token JWT ausente ou inválido", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar este template", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Template não encontrado para o ID informado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "500", description = "Erro interno inesperado no servidor", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
        })
        @GetMapping("/{templateId}")
        public ResponseEntity<ChecklistTemplateResponseDTO> findTemplateById(@PathVariable UUID templateId) {
                return ResponseEntity.ok(mapper.toResponseWithEnrichment(findByIdUseCase.execute(templateId)));
        }

        @Operation(summary = "Listar todos os templates", description = "Retorna a lista completa de templates de checklist cadastrados.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso (pode ser vazia)", content = @Content(schema = @Schema(implementation = ChecklistTemplateResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado — token JWT ausente ou inválido", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para listar templates", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "500", description = "Erro interno inesperado no servidor", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
        })
        @GetMapping
        public ResponseEntity<List<ChecklistTemplateResponseDTO>> listTemplates() {
                return ResponseEntity.ok(mapper.toResponseListWithEnrichment(listUseCase.execute()));
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
        public ResponseEntity<ChecklistTemplateResponseDTO> editTemplate(@PathVariable UUID templateId,
                        @RequestBody @Valid ChecklistTemplateEditRequest request) {
                return ResponseEntity.ok(
                                mapper.toResponseWithEnrichment(editUseCase.execute(templateId, request.toCommand())));
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
                                .body(mapper.toResponseWithEnrichment(createVersionUseCase.execute(templateId)));
        }
}
