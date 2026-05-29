package com.portal.conecta.checklist.shared.context;

/**
 * <h5>Define os tipos de usuário suportados pelo sistema.</h5>
 *
 * <br>
 *
 * Cada tipo representa um perfil com permissões e responsabilidades distintas dentro da plataforma.
 *
 * <br>
 *
 * <table>
 *      <caption> Perfis disponíveis </caption>
 *      <tr><th>Tipo</th><th>Descrição</th></tr>
 *      <tr><td>{@link #STUDENT}</td><td>Aluno matriculado</td></tr>
 *      <tr><td>{@link #REPRESENTATIVE}</td><td>Representante de turma</td></tr>
 *      <tr><td>{@link #TEACHER}</td><td>Professor</td></tr>
 *      <tr><td>{@link #SENAI}</td><td>Funcionário administrativo do SENAI</td></tr>
 *      <tr><td>{@link #WEG}</td><td>Colaborador da WEG</td></tr>
 *      <tr><td>{@link #ADMIN}</td><td>Administrador com acesso total</td></tr>
 * </table>
 */
public enum TypeUser {
    /**
     * Aluno matriculado no curso. Acesso restrito às funcionalidades do aluno.
     */
    STUDENT,
    /**
     * Representante de turma. Possui permissões adicionais em relação ao aluno padrão.
     */
    REPRESENTATIVE,
    /**
     * Professor responsável por turmas e avaliações.
     */
    TEACHER,
    /**
     * Funcionário administrativo do SENAI. Gerencia estrutura institucional.
     */
    SENAI,
    /**
     * Colaborador da WEG. Acesso voltado ao acompanhamento de alunos e turmas.
     */
    WEG,
    /**
     * Administrador do sistema. Acesso irrestrito a todas as funcionalidades.
     */
    ADMIN
}
