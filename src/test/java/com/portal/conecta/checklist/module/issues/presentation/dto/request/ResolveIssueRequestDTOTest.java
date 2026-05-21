package com.portal.conecta.checklist.module.issues.presentation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolveIssueRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should pass validation when resolution notes are valid")
    void shouldPassValidationWhenResolutionNotesAreValid() {
        ResolveIssueRequestDTO dto = new ResolveIssueRequestDTO(
                "EPIs foram fornecidos e colaborador foi orientado. Registro fotográfico anexado."
        );

        Set<ConstraintViolation<ResolveIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should fail validation when resolution notes are null")
    void shouldFailValidationWhenResolutionNotesAreNull() {
        ResolveIssueRequestDTO dto = new ResolveIssueRequestDTO(null);

        Set<ConstraintViolation<ResolveIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("resolutionNotes", violations.iterator().next().getPropertyPath().toString());
        assertEquals("A descrição da resolução é obrigatória.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when resolution notes are blank")
    void shouldFailValidationWhenResolutionNotesAreBlank() {
        ResolveIssueRequestDTO dto = new ResolveIssueRequestDTO("   ");

        Set<ConstraintViolation<ResolveIssueRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().allMatch(v -> v.getPropertyPath().toString().equals("resolutionNotes")));
    }

    @Test
    @DisplayName("should fail validation when resolution notes are too short")
    void shouldFailValidationWhenResolutionNotesAreTooShort() {
        ResolveIssueRequestDTO dto = new ResolveIssueRequestDTO("Resolvido");

        Set<ConstraintViolation<ResolveIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("resolutionNotes", violations.iterator().next().getPropertyPath().toString());
        assertEquals("A resolução deve ter entre 10 e 2000 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when resolution notes exceed maximum length")
    void shouldFailValidationWhenResolutionNotesExceedMaximumLength() {
        ResolveIssueRequestDTO dto = new ResolveIssueRequestDTO("A".repeat(2001));

        Set<ConstraintViolation<ResolveIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("resolutionNotes", violations.iterator().next().getPropertyPath().toString());
        assertEquals("A resolução deve ter entre 10 e 2000 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should pass validation with resolution notes at minimum boundary")
    void shouldPassValidationWithResolutionNotesAtMinimumBoundary() {
        ResolveIssueRequestDTO dto = new ResolveIssueRequestDTO("Resolvido.");

        Set<ConstraintViolation<ResolveIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should pass validation with resolution notes at maximum boundary")
    void shouldPassValidationWithResolutionNotesAtMaximumBoundary() {
        ResolveIssueRequestDTO dto = new ResolveIssueRequestDTO("A".repeat(2000));

        Set<ConstraintViolation<ResolveIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}