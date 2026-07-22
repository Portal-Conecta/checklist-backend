package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.activate.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.create.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.create.CreateChecklistTemplateVersionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.edit.UpdateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.find.FindChecklistTemplateByIdUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.list.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.search.SearchChecklistItemUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.search.SearchItemsByCategoryUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.presentation.dto.template.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.template.response.ChecklistItemByCategorySearchResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.template.response.ChecklistItemSearchResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.template.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.template.request.ChecklistTemplateEditRequest;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import com.portal.conecta.checklist.shared.exception.ApiError;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
@Tag(name = "Checklist Templates", description = "Endpoints para gerenciamento de templates de checklist")
public class ChecklistTemplateController {

    private final SearchChecklistItemUseCase searchChecklistItemUseCase;
    private final SearchItemsByCategoryUseCase searchItemsByCategoryUseCase;
    private final CreateChecklistTemplateUseCase createUseCase;
    private final ActivateChecklistTemplateUseCase activateUseCase;
    private final FindChecklistTemplateByIdUseCase findByIdUseCase;
    private final ListChecklistTemplatesUseCase listUseCase;
    private final UpdateChecklistTemplateUseCase editUseCase;
    private final CreateChecklistTemplateVersionUseCase createVersionUseCase;
    private final ChecklistTemplateMapper mapper;

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
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado — token JWT ausente ou inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para criar templates",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito de integridade — já existe um registro com os mesmos dados únicos",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado no servidor",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @PostMapping
    public ResponseEntity<ChecklistTemplateResponseDTO> createTemplate(@RequestBody @Valid ChecklistTemplateCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseWithEnrichment(createUseCase.execute(request.toCommand())));
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
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado — token JWT ausente ou inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para ativar templates",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Template não encontrado para o ID informado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito de estado — template já está ativo ou não pode ser ativado no status atual",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado no servidor",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @PatchMapping("/{templateId}/activate")
    public ResponseEntity<ChecklistTemplateResponseDTO> activateTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(mapper.toResponseWithEnrichment(activateUseCase.execute(templateId)));
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
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado — token JWT ausente ou inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para visualizar este template",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Template não encontrado para o ID informado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado no servidor",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> findTemplateById(@PathVariable UUID templateId) {
        return ResponseEntity.ok(mapper.toResponseWithEnrichment(findByIdUseCase.execute(templateId)));
    }

    @Operation(
        summary = "Listar templates",
        description = "Retorna a lista de templates de checklist cadastrados. Aceita os parâmetros opcionais " +
            "`roomId` e `status` para filtrar o resultado; quando ausentes, o comportamento é o mesmo de " +
            "listar todos os templates visíveis para o usuário autenticado."
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
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para listar templates",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado no servidor",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<ChecklistTemplateResponseDTO>> listTemplates(
        @Parameter(description = "Filtra os templates pertencentes à sala informada")
        @RequestParam(required = false) UUID roomId,
        @Parameter(description = "Filtra os templates pelo status informado (DRAFT, ACTIVE ou INACTIVE)")
        @RequestParam(required = false) ChecklistTemplateStatus status,
        @Parameter(description = "Filtra os templates pela categoria (grupo de itens da sala)")
        @RequestParam(required = false) ChecklistCategory category
    ) {
        return ResponseEntity.ok(mapper.toResponseListWithEnrichment(listUseCase.execute(roomId, status, category)));
    }

    @Operation(summary = "Editar Template", description = "Atualiza parcialmente um template com status DRAFT. Campos não informados são mantidos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template atualizado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição.", content = @Content),
        @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
        @ApiResponse(responseCode = "403", description = "Usuário sem permissão para editar templates", content = @Content),
        @ApiResponse(responseCode = "404", description = "Template não encontrado.", content = @Content),
        @ApiResponse(responseCode = "409", description = "Template não está em status DRAFT.", content = @Content)
    })
    @PatchMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> editTemplate(@PathVariable UUID templateId, @RequestBody @Valid ChecklistTemplateEditRequest request){
        return ResponseEntity.ok(mapper.toResponseWithEnrichment(editUseCase.execute(templateId, request.toCommand())));
    }

    @Operation(summary = "Criar nova versão", description = "Cria uma nova versão em DRAFT a partir de um template ACTIVE, preservando o histórico")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Nova versão criada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
        @ApiResponse(responseCode = "403", description = "Usuário sem permissão para versionar templates.", content = @Content),
        @ApiResponse(responseCode = "404", description = "Template não encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Template não está em status ACTIVE.", content = @Content)
    })
    @PostMapping("/{templateId}/new-version")
    public ResponseEntity<ChecklistTemplateResponseDTO> createNewVersion(
        @PathVariable UUID templateId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toResponseWithEnrichment(createVersionUseCase.execute(templateId)));
    }

    @Operation(
        summary = "Buscar itens do template por texto",
        description = "Retorna os itens de templates que correspondem ao termo de busca informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Itens encontrados"),
        @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
        @ApiResponse(responseCode = "403", description = "Perfil sem acesso ao template draft", content = @Content),
        @ApiResponse(responseCode = "404", description = "Template não encontrado", content = @Content)
    })
    @GetMapping(value = "/items/search", params = "query")
    public ResponseEntity<List<ChecklistItemSearchResponseDTO>> searchItems(@RequestParam("query") String query) {
        List<ChecklistItemSearchResponseDTO> response = searchChecklistItemUseCase.execute(query)
            .stream()
            .map(mapper::toItemSearchResponseDTO)
            .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Buscar itens por categoria",
        description = "Retorna uma lista de itens de templates ativos que correspondem à categoria fornecida."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Itens encontrados com sucesso (pode ser lista vazia)",
            content = @Content(schema = @Schema(implementation = ChecklistItemByCategorySearchResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado — token JWT ausente ou inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para acessar o módulo de checklist",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado no servidor",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @GetMapping(value = "/items/search", params = "category")
    public ResponseEntity<List<ChecklistItemByCategorySearchResponseDTO>> searchItemsByCategory(@RequestParam("category") String category) {
        List<ChecklistItemByCategorySearchResponseDTO> response = searchItemsByCategoryUseCase.execute(category).stream()
            .map(result -> new ChecklistItemByCategorySearchResponseDTO(
                result.templateId(),
                result.templateTitle(),
                result.sectionKey(),
                result.sectionTitle(),
                result.key(),
                result.title(),
                result.description(),
                result.required(),
                result.order(),
                result.category()))
            .toList();
        return ResponseEntity.ok(response);
    }
}
