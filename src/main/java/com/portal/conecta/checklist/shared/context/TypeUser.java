package com.portal.conecta.checklist.shared.context;

/**
 * <h5>Define os tipos globais de usuario reconhecidos pela Checklist API.</h5>
 *
 * <br>
 *
 * O tipo global e combinado com os papeis por turma para decidir se o usuario
 * pode executar operacoes gerenciais ou operacionais dentro do modulo.
 *
 * <br>
 *
 * <table>
 *      <caption> Perfis disponiveis </caption>
 *      <tr><th>Tipo</th><th>Descricao</th></tr>
 *      <tr><td>{@link #STUDENT}</td><td>Aluno matriculado</td></tr>
 *      <tr><td>{@link #REPRESENTATIVE}</td><td>Representante de turma</td></tr>
 *      <tr><td>{@link #TEACHER}</td><td>Professor</td></tr>
 *      <tr><td>{@link #SENAI}</td><td>Funcionario administrativo do SENAI</td></tr>
 *      <tr><td>{@link #WEG}</td><td>Colaborador da WEG</td></tr>
 *      <tr><td>{@link #ADMIN}</td><td>Administrador com acesso total</td></tr>
 * </table>
 */
public enum TypeUser {
    /**
     * Aluno matriculado no curso. Acesso restrito as funcionalidades do aluno.
     */
    STUDENT,
    /**
     * Representante de turma. Possui permissoes adicionais em relacao ao aluno padrao.
     */
    REPRESENTATIVE,
    /**
     * Professor responsavel por turmas e avaliacoes.
     */
    TEACHER,
    /**
     * Funcionario administrativo do SENAI. Gerencia estrutura institucional.
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
