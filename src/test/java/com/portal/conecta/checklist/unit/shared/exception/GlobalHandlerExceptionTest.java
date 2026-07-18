package com.portal.conecta.checklist.shared.exception;

import com.portal.conecta.checklist.module.checklist.domain.exception.SubmissionWindowViolationException;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalHandlerExceptionTest {

    private GlobalHandlerException handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalHandlerException();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Deve retornar 400 para erro de validação com FieldError presente")
    void testHandleValidationErrorsWithField() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "field", "Campo obrigatorio");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ResponseEntity<ApiError> response = handler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Campo obrigatorio", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 400 para erro de validação sem FieldError")
    void testHandleValidationErrorsWithoutField() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(null);

        ResponseEntity<ApiError> response = handler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Requisicao invalida.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 400 para InvalidRequestException")
    void testHandleInvalidRequest() {
        InvalidRequestException ex = mock(InvalidRequestException.class);
        when(ex.getMessage()).thenReturn("Parametro invalido");

        ResponseEntity<ApiError> response = handler.handleInvalidRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Parametro invalido", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 409 para violação de integridade única (Checklist Duplicado)")
    void testHandleDataIntegrityDuplicateChecklist() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Erro em uidx_execution_no_duplicate");

        ResponseEntity<ApiError> response = handler.handleDataIntegrity(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ja existe checklist ativo para esta turma, sala, periodo, dia e tipo.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 409 para violação de integridade genérica")
    void testHandleDataIntegrityGeneric() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Outro erro de constraint");

        ResponseEntity<ApiError> response = handler.handleDataIntegrity(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ja existe um recurso com estes dados.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 409 para falha de concorrência otimista")
    void testHandleOptimisticLocking() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Stale data");

        ResponseEntity<ApiError> response = handler.handleOptimisticLocking(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("O registro foi alterado por outro usuário. Recarregue os dados.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 503 quando o banco de dados falhar")
    void testHandleDatabaseDown() {
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("DB down");

        ResponseEntity<ApiError> response = handler.handleDatabaseDown(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("O serviço está temporariamente indisponível.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 404 quando entidade não for encontrada")
    void testHandleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Checklist não existe");

        ResponseEntity<ApiError> response = handler.handleEntityNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Checklist não existe", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 404 genérico se EntityNotFoundException não tiver mensagem")
    void testHandleEntityNotFoundNullMessage() {
        EntityNotFoundException ex = new EntityNotFoundException((String) null);

        ResponseEntity<ApiError> response = handler.handleEntityNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso nao encontrado.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 400 para argumento ilegal")
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento invalido");

        ResponseEntity<ApiError> response = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Argumento invalido", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 400 para violação de janela de envio")
    void testHandleWindowViolation() {
        SubmissionWindowViolationException ex = mock(SubmissionWindowViolationException.class);
        when(ex.getMessage()).thenReturn("Fora do horario");

        ResponseEntity<ApiError> response = handler.handleWindowViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Fora do horario", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 409 para estado ilegal")
    void testHandleIllegalState() {
        IllegalStateException ex = new IllegalStateException("Estado invalido");

        ResponseEntity<ApiError> response = handler.handleIllegalState(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Estado invalido", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 403 para acesso negado")
    void testHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access Denied");

        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Usuario sem permissao para executar esta operacao.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 503 para falha de integração com Hub")
    void testHandleHubIntegration() {
        HubIntegrationException ex = mock(HubIntegrationException.class);

        ResponseEntity<ApiError> response = handler.handleHubIntegration(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Ocorreu um erro na integração com o serviço externo.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 400 para tipo de parâmetro incompatível")
    void testHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("idExecution");

        ResponseEntity<ApiError> response = handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Valor inválido para o parâmetro 'idExecution'.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 400 para requisição ilegível (JSON malformado)")
    void testHandleNotReadable() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ApiError> response = handler.handleNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Requisicao invalida.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 405 para método não suportado")
    void testHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
        when(ex.getMethod()).thenReturn("PATCH");

        ResponseEntity<ApiError> response = handler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Método HTTP 'PATCH' não suportado para esta rota.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 404 para rota não encontrada")
    void testHandleNoResourceFound() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ResponseEntity<ApiError> response = handler.handleNoResourceFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso nao encontrado.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve retornar 500 para exceção genérica")
    void testHandleGenericException() {
        Exception ex = new Exception("Erro fatal");

        ResponseEntity<ApiError> response = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocorreu um erro interno.", response.getBody().message());
    }
}
