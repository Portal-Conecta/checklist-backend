package com.portal.conecta.checklist.module.issues.presentation.dto.request;

import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssuePriority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateIssueRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should pass validation when all fields are valid")
    void shouldPassValidationWhenAllFieldsAreValid() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                UUID.randomUUID(),
                "EPI ausente — reincidência",
                "Segunda ocorrência no mesmo posto.",
                IssuePriority.CRITICAL,
                LocalDateTime.now().plusDays(10)
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should pass validation when all fields are null — partial update")
    void shouldPassValidationWhenAllFieldsAreNull() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(null, null, null, null, null);

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should fail validation when title is too short")
    void shouldFailValidationWhenTitleIsTooShort() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                null, "EPI", null, null, null
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("title", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O título deve ter entre 5 e 150 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when title exceeds maximum length")
    void shouldFailValidationWhenTitleExceedsMaximumLength() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                null, "A".repeat(151), null, null, null
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("title", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O título deve ter entre 5 e 150 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when description exceeds maximum length")
    void shouldFailValidationWhenDescriptionExceedsMaximumLength() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                null, null, "A".repeat(2001), null, null
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
        assertEquals("A descrição não pode ultrapassar 2000 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when dueAt is in the past")
    void shouldFailValidationWhenDueAtIsInThePast() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                null, null, null, null, LocalDateTime.now().minusDays(1)
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("dueAt", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O prazo deve ser uma data futura.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should pass validation with title at minimum boundary")
    void shouldPassValidationWithTitleAtMinimumBoundary() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                null, "ABCDE", null, null, null
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should pass validation with title at maximum boundary")
    void shouldPassValidationWithTitleAtMaximumBoundary() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                null, "A".repeat(150), null, null, null
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should pass validation with description at maximum boundary")
    void shouldPassValidationWithDescriptionAtMaximumBoundary() {
        UpdateIssueRequestDTO dto = new UpdateIssueRequestDTO(
                null, null, "A".repeat(2000), null, null
        );

        Set<ConstraintViolation<UpdateIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}