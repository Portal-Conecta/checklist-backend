package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistTemplateFacade;
import com.portal.conecta.checklist.module.checklist.domain.validation.ChecklistTemplateLimits;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "Checklist Templates")
public class ChecklistTemplateController {

    private final ChecklistTemplateFacade checklistTemplateFacade;

    @PostMapping
    @Operation(
            summary = "Cria um template de checklist",
            description = "Limites: corpo da requisicao ate "
                    + ChecklistTemplateLimits.MAX_REQUEST_BODY_DISPLAY
                    + ", ate 20 secoes, ate 50 itens por secao e ate 500 itens no total."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template criado."),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida ou limites do schema excedidos.", content = @Content),
            @ApiResponse(responseCode = "413", description = "Corpo da requisicao acima do limite permitido.", content = @Content)
    })
    public ResponseEntity<ChecklistTemplateResponseDTO> createTemplate(@RequestBody @Valid ChecklistTemplateCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(checklistTemplateFacade.createTemplate(request));
    }

    @PatchMapping("/{templateId}/activate")
    public ResponseEntity<ChecklistTemplateResponseDTO> activateTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(checklistTemplateFacade.activateTemplate(templateId));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> findTemplateById(@PathVariable UUID templateId) {
        return ResponseEntity.ok(checklistTemplateFacade.findTemplateById(templateId));
    }

    @GetMapping
    public ResponseEntity<List<ChecklistTemplateResponseDTO>> listTemplates() {
        return ResponseEntity.ok(checklistTemplateFacade.listTemplates());
    }
}
