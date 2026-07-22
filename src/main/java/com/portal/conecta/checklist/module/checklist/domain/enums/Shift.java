package com.portal.conecta.checklist.module.checklist.domain.enums;

/**
 * Turno da turma informado pelo Hub.
 *
 * <p>Esse dado e usado como base para regras de negocio dependentes do horario
 * real da turma, como janelas de criacao e envio de checklist.</p>
 */
public enum Shift {
    FULL_AM_PM,
    FULL_PM_NT
}
