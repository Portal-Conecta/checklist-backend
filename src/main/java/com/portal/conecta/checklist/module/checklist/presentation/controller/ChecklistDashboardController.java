package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.dto.stats.DashboardStatsResponseDTO;
import com.portal.conecta.checklist.module.checklist.application.usecase.stats.ChecklistDashboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Endpoint composto do dashboard: devolve todos os graficos fixos do painel de
 * checklist numa unica resposta, evitando N requisicoes por tela. Delega ao
 * {@link ChecklistDashboardUseCase} (que orquestra os stats granulares e cacheia
 * o resultado).
 */
@RestController
@RequestMapping("/api/checklist-stats")
@RequiredArgsConstructor
@Tag(name = "Checklist Dashboard", description = "Agregacao composta para o painel de checklist")
public class ChecklistDashboardController {

    private final ChecklistDashboardUseCase dashboardUseCase;

    @Operation(
            summary = "Dashboard composto",
            description = "Retorna, numa unica payload, todos os graficos fixos do painel. "
                    + "Datas ausentes assumem os ultimos 30 dias."
    )
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponseDTO> dashboard(
            @Parameter(description = "Inicio do intervalo (inclusive), formato ISO yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fim do intervalo (inclusive), formato ISO yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(dashboardUseCase.execute(from, to));
    }
}
