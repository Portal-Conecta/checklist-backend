package com.portal.conecta.checklist.module.checklist.presentation.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTokenDeserializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String JSON_COMPLETO = """
            {
              "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
              "nome": "João Silva",
              "email": "joao@exemplo.com",
              "role": "aluno",
              "turmas": [
                {
                  "id": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                  "relacao": "aluno",
                  "papelNaTurma": "representante"
                }
              ],
              "iat": 1710000000,
              "exp": 1710003600
            }
            """;

    @Test
    @DisplayName("Deve deserializar campo 'nome' para o campo 'name' do UserToken")
    void deveDeserializarNomeParaName() throws Exception {
        UserToken token = mapper.readValue(JSON_COMPLETO, UserToken.class);

        assertEquals("João Silva", token.name());
    }

    @Test
    @DisplayName("Deve deserializar campo 'turmas' para o campo 'classList' do UserToken")
    void deveDeserializarTurmasParaClassList() throws Exception {
        UserToken token = mapper.readValue(JSON_COMPLETO, UserToken.class);

        assertNotNull(token.classList());
        assertEquals(1, token.classList().size());
    }

    @Test
    @DisplayName("Deve deserializar todos os campos corretamente")
    void deveDeserializarTodosOsCampos() throws Exception {
        UserToken token = mapper.readValue(JSON_COMPLETO, UserToken.class);

        assertEquals(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), token.id());
        assertEquals("João Silva", token.name());
        assertEquals("joao@exemplo.com", token.email());
        assertEquals("aluno", token.role());
        assertEquals(1710000000L, token.iat());
        assertEquals(1710003600L, token.exp());
    }

    @Test
    @DisplayName("Deve deserializar os dados da turma corretamente")
    void deveDeserializarDadosDaTurma() throws Exception {
        UserToken token = mapper.readValue(JSON_COMPLETO, UserToken.class);

        ClassList turma = token.classList().get(0);
        assertEquals(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), turma.id());
        assertEquals("aluno", turma.relacao());
        assertEquals("representante", turma.papelNaTurma());
    }

    @Test
    @DisplayName("Deve retornar name nulo quando JSON não contém campo 'nome'")
    void deveRetornarNameNuloSemCampoNome() throws Exception {
        String jsonSemNome = """
                {
                  "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                  "email": "joao@exemplo.com",
                  "role": "aluno",
                  "turmas": [],
                  "iat": 1710000000,
                  "exp": 1710003600
                }
                """;

        UserToken token = mapper.readValue(jsonSemNome, UserToken.class);

        assertNull(token.name());
    }
}
