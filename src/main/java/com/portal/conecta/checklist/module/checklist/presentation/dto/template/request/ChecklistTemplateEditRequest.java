package com.portal.conecta.checklist.module.checklist.presentation.dto.template.request;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.edit.UpdateChecklistTemplateCommand;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistSchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado para atualização parcial de um template de checklist.
 *
 * <p>Todos os campos são opcionais. Quando um campo é informado como
 * {@code null}, o valor atualmente armazenado no template é mantido.</p>
 */
public record ChecklistTemplateEditRequest(

        @Pattern(
                regexp = "^(?!\\s*$).+",
                message = "title não pode ser vazio ou conter apenas espaços."
        )
        @Size(max = 150, message = "title deve ter no máximo 150 caracteres.")
        String title,

        @Size(max = 250, message = "description deve ter no maximo 250 caracteres.")
        String description,

        ChecklistCategory category,

        @Valid
        ChecklistSchema schemaJson
) {
    public UpdateChecklistTemplateCommand toCommand() {
        return new UpdateChecklistTemplateCommand(title, description, category, schemaJson);
    }
}
