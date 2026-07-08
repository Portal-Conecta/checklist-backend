package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubmissionWindowController.class)
class SubmissionWindowAuthorizationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean Object upsertSubmissionWindowUseCase;
    @MockitoBean Object listSubmissionWindowsUseCase;

    @Test
    @WithAnonymousUser
    void requestsSemTokenDevemRetornar401() throws Exception {
        mockMvc.perform(get("/api/submission-windows")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/submission-windows").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_STUDENT", "ROLE_TEACHER", "ROLE_SENAI"})
    void qualquerUsuarioAutenticadoDeveListarJanelas() throws Exception {
        mockMvc.perform(get("/api/submission-windows"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_SENAI") // Representando perfil gerencial
    void gerentesDevemConfigurarJanela() throws Exception {
        mockMvc.perform(post("/api/submission-windows")
                        .contentType(APPLICATION_JSON)
                        .content("{ \"payload\": \"valido\" }"))
                .andExpect(status().isOk()); // ou isCreated() dependendo do seu retorno
    }

    @Test
    @WithMockUser(authorities = {"ROLE_TEACHER", "ROLE_REPRESENTATIVE", "ROLE_STUDENT", "ROLE_ADMIN"})
    void operacionaisEOutrosNaoDevemConfigurarJanela() throws Exception {
        mockMvc.perform(post("/api/submission-windows")
                        .contentType(APPLICATION_JSON)
                        .content("{ \"payload\": \"valido\" }"))
                .andExpect(status().isForbidden());
    }
}