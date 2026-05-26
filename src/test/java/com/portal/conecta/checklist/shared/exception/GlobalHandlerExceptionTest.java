package com.portal.conecta.checklist.shared.exception;

import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalHandlerExceptionTest {

    private final GlobalHandlerException handler = new GlobalHandlerException();

    @Test
    @DisplayName("should return bad request with field errors when validation fails")
    void shouldReturnBadRequestWithFieldErrorsWhenValidationFails() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError nameError = new FieldError("request", "name", "Name is required");
        FieldError descriptionError = new FieldError("request", "description", "Description is required");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nameError, descriptionError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationErrors(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Erro de validação nos campos informados.", response.getBody().message());
        assertEquals("Name is required", response.getBody().errors().get("name"));
        assertEquals("Description is required", response.getBody().errors().get("description"));
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return conflict when data integrity violation occurs")
    void shouldReturnConflictWhenDataIntegrityViolationOccurs() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("duplicated value"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflito de integridade: o registro já existe ou viola regras de negócio.", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return conflict when optimistic locking fails")
    void shouldReturnConflictWhenOptimisticLockingFails() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleOptimisticLocking(new OptimisticLockingFailureException("stale data"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("O registro foi alterado por outro usuário. Por favor, recarregue os dados e tente novamente.", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return service unavailable when database is down")
    void shouldReturnServiceUnavailableWhenDatabaseIsDown() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleDatabaseDown(new DataAccessResourceFailureException("database unavailable"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("O serviço de banco de dados está temporariamente indisponível.", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return not found when entity does not exist")
    void shouldReturnNotFoundWhenEntityDoesNotExist() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleEntityNotFound(new EntityNotFoundException("Checklist not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Checklist not found", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return forbidden when user has no permission")
    void shouldReturnForbiddenWhenUserHasNoPermission() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleAccessDenied(new AccessDeniedException("Access denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("Access denied", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return service unavailable when Hub integration fails")
    void shouldReturnServiceUnavailableWhenHubIntegrationFails() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleHubIntegration(new HubIntegrationException("Hub unavailable"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("Hub unavailable", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return internal server error for generic exceptions")
    void shouldReturnInternalServerErrorForGenericExceptions() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleGenericException(new RuntimeException("unexpected failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Ocorreu um erro interno inesperado no servidor.", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }
}
