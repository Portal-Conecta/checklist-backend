package com.portal.conecta.checklist.module.checklist.domain.enums;

/**
 * Tipo de resposta esperado para um item do checklist.
 *
 * <p>Define o formato da resposta que o preenchedor deve fornecer
 * para cada item durante a execução do checklist.</p>
 */
public enum AnswerType {

    /**
     * Resposta de conformidade: CONFORME ou NAO_CONFORME.
     */
    CONFORMITY,

    /**
     * Resposta em texto livre.
     */
    TEXT,

    /**
     * Resposta numérica.
     */
    NUMBER
}
