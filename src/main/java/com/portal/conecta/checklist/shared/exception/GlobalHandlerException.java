    package com.portal.conecta.checklist.shared.exception;

    import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
    import jakarta.persistence.EntityNotFoundException;
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

    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.Map;

    /**
     * Gerenciador Global de Exceções (Controller Advice)
     * <p>
     * Intercepta as exceções lançadas pelos Controllers de toda a aplicação,
     * padronizando o retorno para o cliente através do objeto {@link ErrorResponseDTO}.
     * </p>
     */
    @RestControllerAdvice
    public class GlobalHandlerException {
        /**
         * Trata erros de validação de campos ( anotações como {@code @NotNull}, {@code @@size}, etc.)
         * <p><b>Status HTTP:</b> 400 - Bad Request</p>
         *
         * @param ex A exceção {@link MethodArgumentNotValidException} que contem os erros de validação.
          * @return Resposta Detalhada  com um mapa que contem os campos invalidos e seus motivos.
         */

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex) {
            Map<String, String> errors = new HashMap<>();
            ex.getBindingResult().getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );

            return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação nos campos informados.", errors);
        }

        /**
         * Trata violações de integridade no banco de dados, como tentativas de inserir registros duplicados.
         * <p><b>Status HTTP:</b> 409 - Conflict</p>
         *
         * @param ex A exceção {@link DataIntegrityViolationException}.
         * @return Resposta de conflito com mensagem amigável.
         */
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
            return buildResponse(HttpStatus.CONFLICT, "Conflito de integridade: o registro já existe ou viola regras de negócio.", null);
        }
        /**
         * Trata falhas de concorrência otimista quando dois usuários tentam atualizar o mesmo registro ao mesmo tempo.
         * <p><b>Status HTTP:</b> 409 - Conflict</p>
         *
         * @param ex A exceção {@link OptimisticLockingFailureException}.
         * @return Resposta orientando o usuário a recarregar os dados da tela.
         */
        @ExceptionHandler(OptimisticLockingFailureException.class)
        public ResponseEntity<ErrorResponseDTO> handleOptimisticLocking(OptimisticLockingFailureException ex) {
            return buildResponse(HttpStatus.CONFLICT, "O registro foi alterado por outro usuário. Por favor, recarregue os dados e tente novamente.", null);
        }
        /**
         * Trata a indisponibilidade física ou falha crítica de conexão com o banco de dados.
         * <p><b>Status HTTP:</b> 503 - Service Unavailable</p>
         *
         * @param ex A exceção {@link DataAccessResourceFailureException}.
         * @return Resposta indicando que o banco está temporariamente fora do ar.
         */
        @ExceptionHandler(DataAccessResourceFailureException.class)
        public ResponseEntity<ErrorResponseDTO> handleDatabaseDown(DataAccessResourceFailureException ex) {
            return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "O serviço de banco de dados está temporariamente indisponível.", null);
        }

        /**
         * Trata a ausência de um registro solicitado (ex: buscar um ID que não existe).
         * <p><b>Status HTTP:</b> 404 - Not Found</p>
         *
         * @param ex A exceção {@link EntityNotFoundException}.
         * @return Resposta com a mensagem original detalhando o recurso ausente.
         */
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponseDTO> handleEntityNotFound(EntityNotFoundException ex) {
            return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
        }
        /**
         * Trata argumentos ilegais ou inválidos passados para os métodos de negócio.
         * <p><b>Status HTTP:</b> 400 - Bad Request</p>
         *
         * @param ex A exceção {@link IllegalArgumentException}.
         * @return Resposta contendo o motivo da rejeição do argumento.
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
            return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }
        /**
         * Trata estados ilegais na execução do fluxo de negócio (regras de negócio violadas).
         * <p><b>Status HTTP:</b> 409 - Conflict</p>
         *
         * @param ex A exceção {@link IllegalStateException}.
         * @return Resposta indicando a quebra da regra de negócio atual.
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponseDTO> handleIllegalState(IllegalStateException ex) {
            return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
        }
        /**
         * Trata tentativas de acesso a recursos sem as permissões ou perfis necessários.
         * <p><b>Status HTTP:</b> 403 - Forbidden</p>
         *
         * @param ex A exceção de segurança {@link AccessDeniedException}.
         * @return Resposta restrita de acesso negado.
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
            return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
        }
        /**
         * Trata falhas de integração específicas com o ecossistema externo do Hub.
         * <p><b>Status HTTP:</b> 503 - Service Unavailable</p>
         *
         * @param ex A exceção customizada {@link HubIntegrationException}.
         * @return Resposta informando que o serviço integrado falhou ou está indisponível.
         */
        @ExceptionHandler(HubIntegrationException.class)
        public ResponseEntity<ErrorResponseDTO> handleHubIntegration(HubIntegrationException ex) {
            return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
        }
        /**
         * Trata erros de digitação/incompatibilidade nos parâmetros da URL (ex: passar texto onde se espera um ID numérico).
         * <p><b>Status HTTP:</b> 400 - Bad Request</p>
         *
         * @param ex A exceção {@link MethodArgumentTypeMismatchException}.
         * @return Resposta dinâmica informando qual parâmetro está incorreto.
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
            String message = "Valor inválido para o parâmetro '%s': '%s'".formatted(ex.getName(), ex.getValue());
            return buildResponse(HttpStatus.BAD_REQUEST, message, null);
        }
        /**
         * Trata corpos de requisição (JSON) malformados, com erros de sintaxe ou valores impossíveis de mapear.
         * <p><b>Status HTTP:</b> 400 - Bad Request</p>
         *
         * @param ex A exceção {@link HttpMessageNotReadableException}.
         * @return Resposta de erro de leitura do JSON.
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponseDTO> handleNotReadable(HttpMessageNotReadableException ex) {
            return buildResponse(HttpStatus.BAD_REQUEST, "JSON malformado ou valor inválido no corpo da requisição.", null);
        }
        /**
         * Trata chamadas de endpoints usando métodos HTTP não configurados para a rota (ex: dar um POST em rota GET).
         * <p><b>Status HTTP:</b> 405 - Method Not Allowed</p>
         *
         * @param ex A exceção {@link HttpRequestMethodNotSupportedException}.
         * @return Resposta informando o método inadequado.
         */
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
            String message = "Método HTTP '%s' não suportado para esta rota.".formatted(ex.getMethod());
            return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, message, null);
        }
        /**
         * Trata cenários onde a URL digitada não corresponde a nenhuma rota ou recurso físico mapeado na API.
         * <p><b>Status HTTP:</b> 404 - Not Found</p>
         *
         * @param ex A exceção {@link NoResourceFoundException}.
         * @return Resposta informando a rota inexistente buscada.
         */
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ErrorResponseDTO> handleNoResourceFound(NoResourceFoundException ex) {
            return buildResponse(HttpStatus.NOT_FOUND, "Rota não encontrada: " + ex.getResourcePath(), null);
        }
        /**
         * Captura qualquer outra exceção não tratada explicitamente nesta classe.
         * <p><b>Status HTTP:</b> 500 - Internal Server Error</p>
         * <p><i>Atenção: Funciona como uma rede de segurança para evitar o vazamento de stacktraces para o cliente.</i></p>
         *
         * @param ex A exceção genérica {@link Exception}.
         * @return Resposta genérica e segura de falha interna do servidor.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado no servidor.", null);
        }
        /**
         * Método auxiliar privado para montagem e encapsulamento do {@link ResponseEntity}.
         *
         * @param status  O enum {@link HttpStatus} da resposta.
         * @param message A mensagem descritiva do erro.
         * @param errors  O mapa opcional com detalhes específicos dos campos.
         * @return A instância final de {@link ResponseEntity} contendo o DTO de erro.
         */
        private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String message, Map<String, String> errors) {
            ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    status.value(),
                    message,
                    errors
            );
            return ResponseEntity.status(status).body(errorResponseDTO);
        }
    }
