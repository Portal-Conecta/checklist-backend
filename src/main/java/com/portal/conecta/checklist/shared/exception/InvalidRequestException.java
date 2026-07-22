package com.portal.conecta.checklist.shared.exception;

/**
 * Exceção lançada quando o cliente envia parâmetros inválidos na requisição.
 *
 * <p>Representa exclusivamente erros de validação de entrada originados do lado
 * do cliente (parâmetros de query, path variables ou corpo da requisição fora
 * dos valores esperados).</p>
 *
 * <p>Difere de {@link IllegalArgumentException} — esta última é reservada para
 * erros internos da API Java, enquanto {@code InvalidRequestException} sinaliza
 * explicitamente uma requisição malformada que deve ser corrigida pelo cliente.</p>
 *
 * <p><b>Status HTTP esperado:</b> 400 - Bad Request</p>
 *
 * @see GlobalHandlerException#handleInvalidRequest(InvalidRequestException, jakarta.servlet.http.HttpServletRequest)
 */
public class InvalidRequestException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descritiva do erro de validação.
     *
     * @param message descrição clara do parâmetro inválido e dos valores aceitos
     */
    public InvalidRequestException(String message) {
        super(message);
    }
}