package com.portal.conecta.checklist.shared.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**<h1>DTO para erros </h1>
 * <p>
 * Essa classe vai certralizar informações criticas de uma falha que facilita
 * o tratamento de erros de que consumir endpoints
 * </p>
 *  <p><b>Exemplo de estrutura JSON gerada:</b></p>
 *  <pre>
 *      {
 *          "locaDateTime": "2026-0529T19:40:00",
 *          "status": 400,
 *          "message":  "Erro de validação",
 *          "errors": {"email": "E-mail inválido"}
 *      }
 *  </pre>
 * @param localDateTime Tempo exato de quando ocorreu o erro.
 * @param status Código de status HTTP da requisição (ex: 400, 404, 500).
 * @param message Uma mensagem geral para descrever erros.
 * @param errors Um mapa que contem detalhes especificos dos erros.
 */
public record ErrorResponseDTO(
        LocalDateTime localDateTime,
        int status,
        String message,
        Map<String, String> errors
) {
}