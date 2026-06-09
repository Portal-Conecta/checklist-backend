package com.portal.conecta.checklist.module.checklist.presentation.dto.update;

import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO utilizado para atualização parcial de um template de checklist.
 *
 * <p>Todos os campos são opcionais. Quando um campo é informado como
 * {@code null}, o valor atualmente armazenado no template é mantido.</p>
 *
 * @param roomId identificador da sala associada ao template
 * @param title título do template
 * @param description descrição do template
 * @param schemaJson estrutura do checklist contendo seções, campos e regras de validação
 */
public record ChecklistTemplateEditRequest(

        UUID roomId,
        /**
         *
         */
        @Pattern(
                regexp = "^(?!\\s*$).+",
                message = "title não pode ser vazio ou conter apenas espaços."
        )
        @Size(max = 150, message = "title deve ter no máximo 150 caracteres.")
        String title,

        @Size(max = 250, message = "description deve ter no maximo 250 caracteres.")
        String description,

        @Valid
        ChecklistSchemaDTO schemaJson
) {
}
