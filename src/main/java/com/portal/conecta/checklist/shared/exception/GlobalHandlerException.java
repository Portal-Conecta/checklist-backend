package com.portal.conecta.checklist.shared.exception;

import com.portal.conecta.checklist.modules.checklist.domain.exception.SubmissionWindowViolationException;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

/**
 * Gerenciador Global de Exceções (Controller Advice)
 * <p>
 * Intercepta as exceções lançadas pelos Controllers de toda a aplicação,
 * padronizando o retorno para o cliente através do objeto {@link ApiError}.
 * </p>
 */
@RestControllerAdvice
public class GlobalHandlerException {
    private static final String DUPLICATE_CHECKLIST_INDEX = "uidx_execution_no_duplicate";

    /**
     * Trata erros de validação de campos ( anotações como {@code @NotNull}, {@code @@size}, etc.)
     * <p><b>Status HTTP:</b> 400 - Bad Request</p>
     *
     * @param ex A exceção {@link MethodArgumentNotValidException} que contem os erros de validação.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta Detalhada com a mensagem do primeiro campo inválido mapeado.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Requisicao invalida.";

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Trata violações de integridade no banco de dados, como tentativas de inserir registros duplicados.
     * <p><b>Status HTTP:</b> 409 - Conflict</p>
     *
     * @param ex A exceção {@link DataIntegrityViolationException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta de conflito com mensagem amigável.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        if (containsIgnoreCase(ex, DUPLICATE_CHECKLIST_INDEX)) {
            return buildResponse(
                    HttpStatus.CONFLICT,
                    "Ja existe checklist ativo para esta turma, sala, periodo, dia e tipo.",
                    request
            );
        }

        return buildResponse(HttpStatus.CONFLICT, "Ja existe um recurso com estes dados.", request);
    }

    /**
     * Trata falhas de concorrência otimista quando dois usuários tentam atualizar o mesmo registro ao mesmo tempo.
     * <p><b>Status HTTP:</b> 409 - Conflict</p>
     *
     * @param ex A exceção {@link OptimisticLockingFailureException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta orientando o usuário a recarregar os dados da tela.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLocking(OptimisticLockingFailureException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "O registro foi alterado por outro usuário. Recarregue os dados.", request);
    }

    /**
     * Trata a indisponibilidade física ou falha crítica de conexão com o banco de dados.
     * <p><b>Status HTTP:</b> 503 - Service Unavailable</p>
     *
     * @param ex A exceção {@link DataAccessResourceFailureException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta indicando que o banco está temporariamente fora do ar.
     */
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiError> handleDatabaseDown(DataAccessResourceFailureException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "O serviço está temporariamente indisponível.", request);
    }

    /**
     * Trata a ausência de um registro solicitado (ex: buscar um ID que não existe).
     * <p><b>Status HTTP:</b> 404 - Not Found</p>
     *
     * @param ex A exceção {@link EntityNotFoundException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta com a mensagem original detalhando o recurso ausente.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Recurso nao encontrado.", request);
    }

    /**
     * Trata argumentos ilegais ou inválidos passados para os métodos de negócio.
     * <p><b>Status HTTP:</b> 400 - Bad Request</p>
     *
     * @param ex A exceção {@link IllegalArgumentException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta contendo o motivo da rejeição do argumento.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Trata estados ilegais na execução do fluxo de negócio (regras de negócio violadas).
     * <p><b>Status HTTP:</b> 409 - Conflict</p>
     *
     * @param ex A exceção {@link IllegalStateException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta indicando a quebra da regra de negócio atual.
     */
    @ExceptionHandler(SubmissionWindowViolationException.class)
    public ResponseEntity<ApiError> handleWindowViolation(SubmissionWindowViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Trata tentativas de acesso a recursos sem as permissões ou perfis necessários.
     * <p><b>Status HTTP:</b> 403 - Forbidden</p>
     *
     * @param ex A exceção de segurança {@link AccessDeniedException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta restrita de acesso negado.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Usuario sem permissao para executar esta operacao.", request);
    }

    /**
     * Trata falhas de integração específicas com o ecossistema externo do Hub.
     * <p><b>Status HTTP:</b> 503 - Service Unavailable</p>
     *
     * @param ex A exceção customizada {@link HubIntegrationException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta informando que o serviço integrado falhou ou está indisponível.
     */
    @ExceptionHandler(HubIntegrationException.class)
    public ResponseEntity<ApiError> handleHubIntegration(HubIntegrationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Ocorreu um erro na integração com o serviço externo.", request);
    }

    /**
     * Trata erros de digitação/incompatibilidade nos parâmetros da URL (ex: passar texto onde se espera um ID numérico).
     * <p><b>Status HTTP:</b> 400 - Bad Request</p>
     *
     * @param ex A exceção {@link MethodArgumentTypeMismatchException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta dinâmica informando qual parâmetro está incorreto.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Valor inválido para o parâmetro '%s'.".formatted(ex.getName());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Trata corpos de requisição (JSON) malformados, com erros de sintaxe ou valores impossíveis de mapear.
     * <p><b>Status HTTP:</b> 400 - Bad Request</p>
     *
     * @param ex A exceção {@link HttpMessageNotReadableException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta de erro de leitura do JSON.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Requisicao invalida.", request);
    }

    /**
     * Trata chamadas de endpoints usando métodos HTTP não configurados para a rota (ex: dar um POST em rota GET).
     * <p><b>Status HTTP:</b> 405 - Method Not Allowed</p>
     *
     * @param ex A exceção {@link HttpRequestMethodNotSupportedException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta informando o método inadequado.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = "Método HTTP '%s' não suportado para esta rota.".formatted(ex.getMethod());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request);
    }

    /**
     * Trata cenários onde a URL digitada não corresponde a nenhuma rota ou recurso físico mapeado na API.
     * <p><b>Status HTTP:</b> 404 - Not Found</p>
     *
     * @param ex A exceção {@link NoResourceFoundException}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta informando a rota inexistente buscada.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso nao encontrado.", request);
    }

    /**
     * Captura qualquer outra exceção não tratada explicitamente nesta classe.
     * <p><b>Status HTTP:</b> 500 - Internal Server Error</p>
     * <p><i>Atenção: Funciona como uma rede de segurança para evitar o vazamento de stacktraces para o cliente.</i></p>
     *
     * @param ex A exceção genérica {@link Exception}.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return Resposta genérica e segura de falha interna do servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno.", request);
    }

    /**
     * Método auxiliar privado para montagem e encapsulamento do {@link ResponseEntity}.
     *
     * @param status  O enum {@link HttpStatus} da resposta.
     * @param message A mensagem descritiva do erro.
     * @param request O objeto {@link HttpServletRequest} contendo os detalhes da rota acessada.
     * @return A instância final de {@link ResponseEntity} contendo o DTO de erro.
     */
    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ApiError apiError = new ApiError(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(apiError);
    }

    private boolean containsIgnoreCase(Throwable throwable, String expected) {
        Throwable current = throwable;

        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains(expected.toLowerCase())) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }
}