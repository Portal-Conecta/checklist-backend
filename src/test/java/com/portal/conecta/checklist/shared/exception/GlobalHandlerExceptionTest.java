package com.portal.conecta.checklist.shared.exception;

import com.portal.conecta.checklist.modules.checklist.domain.exception.SubmissionWindowViolationException;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalHandlerExceptionTest {

    @InjectMocks
    private GlobalHandlerException handler;

    @Mock
    private HttpServletRequest request;

    private static final String TEST_PATH = "/api/v1/test-route";

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(TEST_PATH);
    }

    @Test
    @DisplayName("should return bad request with first field error message when validation fails")
    void shouldReturnBadRequestWithFieldErrorsWhenValidationFails() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError nameError = new FieldError("request", "name", "Nome e obrigatorio");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(nameError);

        ResponseEntity<ApiError> response = handler.handleValidationErrors(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Nome e obrigatorio", response.getBody().message());
        assertEquals(TEST_PATH, response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("should return conflict when generic data integrity violation occurs")
    void shouldReturnConflictWhenDataIntegrityViolationOccurs() {
        ResponseEntity<ApiError> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("duplicated value"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Ja existe um recurso com estes dados.", response.getBody().message());
        assertEquals(TEST_PATH, response.getBody().path());
    }

    @Test
    @DisplayName("should return conflict with specific duplicate checklist message when unique index fails")
    void shouldReturnConflictWithDuplicateChecklistMessageWhenUniqueIndexFails() {
        ResponseEntity<ApiError> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uidx_execution_no_duplicate\""
                ), request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Ja existe checklist ativo para esta turma, sala, periodo, dia e tipo.", response.getBody().message());
    }

    @Test
    @DisplayName("should return conflict when optimistic locking fails")
    void shouldReturnConflictWhenOptimisticLockingFails() {
        ResponseEntity<ApiError> response =
                handler.handleOptimisticLocking(new OptimisticLockingFailureException("stale data"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("O registro foi alterado por outro usuário. Recarregue os dados.", response.getBody().message());
    }

    @Test
    @DisplayName("should return bad request when submission window is closed")
    void shouldReturnBadRequestWhenSubmissionWindowIsClosed() {
        ResponseEntity<ApiError> response = handler.handleWindowViolation(
                new SubmissionWindowViolationException(LocalTime.of(7, 30), LocalTime.of(8, 0)), request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Fora da janela de envio. Janela permitida: 07:30 ate 08:00.", response.getBody().message());
    }

    @Test
    @DisplayName("should return conflict when illegal state exception occurs")
    void shouldReturnConflictWhenIllegalStateExceptionOccurs() {
        ResponseEntity<ApiError> response = handler.handleIllegalState(
                new IllegalStateException("Regra de negócio violada"), request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Regra de negócio violada", response.getBody().message());
    }

    @Test
    @DisplayName("should return bad request when illegal argument exception occurs")
    void shouldReturnBadRequestWhenIllegalArgumentExceptionOccurs() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Argumento invalido"), request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Argumento invalido", response.getBody().message());
    }

    @Test
    @DisplayName("should return service unavailable when database is down")
    void shouldReturnServiceUnavailableWhenDatabaseIsDown() {
        ResponseEntity<ApiError> response =
                handler.handleDatabaseDown(new DataAccessResourceFailureException("database unavailable"), request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("O serviço está temporariamente indisponível.", response.getBody().message());
    }

    @Test
    @DisplayName("should return not found when entity does not exist")
    void shouldReturnNotFoundWhenEntityDoesNotExist() {
        ResponseEntity<ApiError> response =
                handler.handleEntityNotFound(new EntityNotFoundException("Checklist nao encontrado"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Checklist nao encontrado", response.getBody().message());
    }

    @Test
    @DisplayName("should return forbidden when user has no permission")
    void shouldReturnForbiddenWhenUserHasNoPermission() {
        ResponseEntity<ApiError> response =
                handler.handleAccessDenied(new AccessDeniedException("Acesso negado"), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("Forbidden", response.getBody().error());
        assertEquals("Usuario sem permissao para executar esta operacao.", response.getBody().message());
    }

    @Test
    @DisplayName("should return service unavailable when Hub integration fails")
    void shouldReturnServiceUnavailableWhenHubIntegrationFails() {
        ResponseEntity<ApiError> response =
                handler.handleHubIntegration(new HubIntegrationException("Hub indisponivel"), request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("Ocorreu um erro na integração com o serviço externo.", response.getBody().message());
    }

    @Test
    @DisplayName("should return internal server error for generic exceptions")
    void shouldReturnInternalServerErrorForGenericExceptions() {
        ResponseEntity<ApiError> response =
                handler.handleGenericException(new RuntimeException("unexpected failure"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Ocorreu um erro interno.", response.getBody().message());
    }

    @Test
    @DisplayName("should return bad request when UUID or type param is invalid")
    void shouldReturnBadRequestWhenUuidOrTypeParamIsInvalid() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("executionId");

        ResponseEntity<ApiError> response = handler.handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Valor inválido para o parâmetro 'executionId'.", response.getBody().message());
    }

    @Test
    @DisplayName("should return bad request when JSON is malformed or invalid")
    void shouldReturnBadRequestWhenJsonIsMalformed() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ApiError> response = handler.handleNotReadable(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Requisicao invalida.", response.getBody().message());
    }

    @Test
    @DisplayName("should return method not allowed when HTTP method is not supported")
    void shouldReturnMethodNotAllowedWhenHttpMethodIsNotSupported() {
        HttpRequestMethodNotSupportedException exception = mock(HttpRequestMethodNotSupportedException.class);
        when(exception.getMethod()).thenReturn("DELETE");

        ResponseEntity<ApiError> response = handler.handleMethodNotSupported(exception, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(405, response.getBody().status());
        assertEquals("Método HTTP 'DELETE' não suportado para esta rota.", response.getBody().message());
    }

    @Test
    @DisplayName("should return not found when route does not exist")
    void shouldReturnNotFoundWhenRouteDoesNotExist() {
        NoResourceFoundException exception = mock(NoResourceFoundException.class);

        ResponseEntity<ApiError> response = handler.handleNoResourceFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Recurso nao encontrado.", response.getBody().message());
    }
}