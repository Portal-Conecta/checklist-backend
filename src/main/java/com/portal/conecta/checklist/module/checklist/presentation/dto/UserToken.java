package com.portal.conecta.checklist.module.checklist.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record UserToken(
        UUID id,
        @JsonProperty("nome") String name,
        String email,
        String role,
        @JsonProperty("turmas") List<ClassList> classList,
        Long iat,
        Long exp) {
}
