package com.portal.conecta.checklist.shared.exception;

import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import com.portal.conecta.checklist.modules.checklist.domain.exception.SubmissionWindowViolationException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.time.LocalTime;

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
        FieldError nameError = new FieldError("request", "name", "Nome e obrigatorio");
        FieldError descriptionError = new FieldError("request", "description", "Descricao e obrigatoria");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nameError, descriptionError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationErrors(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Erro de validação nos campos informados.", response.getBody().message());
        assertEquals("Nome e obrigatorio", response.getBody().errors().get("name"));
        assertEquals("Descricao e obrigatoria", response.getBody().errors().get("description"));
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
    @DisplayName("should return conflict with duplicate checklist message when unique index fails")
    void shouldReturnConflictWithDuplicateChecklistMessageWhenUniqueIndexFails() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uidx_execution_no_duplicate\""
                )
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Ja existe checklist ativo para esta turma, sala, periodo, dia e tipo.", response.getBody().message());
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
    @DisplayName("should return unprocessable entity when submission window is closed")
    void shouldReturnUnprocessableEntityWhenSubmissionWindowIsClosed() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleWindowViolation(
                new SubmissionWindowViolationException(LocalTime.of(7, 30), LocalTime.of(8, 0))
        );

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().status());
        assertEquals("Fora da janela de envio. Janela permitida: 07:30 ate 08:00.", response.getBody().message());
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
                handler.handleEntityNotFound(new EntityNotFoundException("Checklist nao encontrado"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Checklist nao encontrado", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return forbidden when user has no permission")
    void shouldReturnForbiddenWhenUserHasNoPermission() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleAccessDenied(new AccessDeniedException("Acesso negado"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("Acesso negado", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return service unavailable when Hub integration fails")
    void shouldReturnServiceUnavailableWhenHubIntegrationFails() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleHubIntegration(new HubIntegrationException("Hub indisponivel"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("Hub indisponivel", response.getBody().message());
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
    @Test
    @DisplayName("should return bad request when UUID or type param is invalid")
    void shouldReturnBadRequestWhenUuidOrTypeParamIsInvalid() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("executionId");
        when(exception.getValue()).thenReturn("uuid-invalido");

        ResponseEntity<ErrorResponseDTO> response = handler.handleTypeMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Valor inválido para o parâmetro 'executionId': 'uuid-invalido'", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return bad request when JSON is malformed or enum is invalid")
    void shouldReturnBadRequestWhenJsonIsMalformedOrEnumIsInvalid() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponseDTO> response = handler.handleNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("JSON malformado ou valor inválido no corpo da requisição.", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return method not allowed when HTTP method is not supported")
    void shouldReturnMethodNotAllowedWhenHttpMethodIsNotSupported() {
        HttpRequestMethodNotSupportedException exception = mock(HttpRequestMethodNotSupportedException.class);
        when(exception.getMethod()).thenReturn("DELETE");

        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodNotSupported(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(405, response.getBody().status());
        assertEquals("Método HTTP 'DELETE' não suportado para esta rota.", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }

    @Test
    @DisplayName("should return not found when route does not exist")
    void shouldReturnNotFoundWhenRouteDoesNotExist() {
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getResourcePath()).thenReturn("/api/rota-inexistente");

        ResponseEntity<ErrorResponseDTO> response = handler.handleNoResourceFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Rota não encontrada: /api/rota-inexistente", response.getBody().message());
        assertNull(response.getBody().errors());
        assertNotNull(response.getBody().localDateTime());
    }
}
