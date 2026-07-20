package com.portal.conecta.checklist.shared.config;

import com.portal.conecta.checklist.shared.integration.hub.config.HubApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Popula os dados de dev necessarios pra realizar um checklist de ponta a
 * ponta, no perfil {@code dev}: um template ATIVO por sala do Hub e janelas
 * de envio (submission windows) pras turmas de teste.
 *
 * <p>O checklist-backend nao possui turmas nem salas em seu proprio banco
 * (sao do Hub), entao este seeder autentica como o admin de dev ja semeado
 * pelo {@code core-backend} e usa a API HTTP (Hub pra listar salas/turmas, o
 * proprio servico via loopback pra criar templates e janelas) em vez de
 * chamar os casos de uso diretamente -- eles exigem um contexto de requisicao
 * autenticado que nao existe durante o startup.</p>
 *
 * <p>Deixa a turma ADSIS2 de proposito sem janela, para o estado "sem janela
 * configurada" continuar testavel sem precisar desfazer nada manualmente.
 * Templates sao idempotentes (pula a sala se ja existir template ATIVO).
 * Falhas aqui (Hub fora do ar, admin nao encontrado) sao logadas como aviso e
 * nao impedem a subida do servico -- e conveniencia de dev, nao requisito.</p>
 *
 * <p><strong>Ativo apenas no perfil {@code dev}. Nao deve ser executado em
 * producao.</strong></p>
 */
@Configuration
@Profile("dev")
public class ChecklistDevSeedInitializer {

    private static final Logger log = LoggerFactory.getLogger(ChecklistDevSeedInitializer.class);

    private static final String DEV_ADMIN_EMAIL = "admin@portal.test";
    private static final String DEV_ADMIN_PASSWORD = "123456";

    private static final List<String> CLASS_NAMES_TO_SEED = List.of("MIDS1", "ADSIS1", "MIDS2");

    @Bean
    public CommandLineRunner seedChecklistDevData(
        HubApiProperties hubApiProperties,
        @Value("${server.port:8080}") int serverPort
    ) {
        return args -> {
            RestClient hubClient = RestClient.create(hubApiProperties.url());
            RestClient selfClient = RestClient.create("http://localhost:" + serverPort);

            String token;
            try {
                token = login(hubClient);
            } catch (Exception exception) {
                log.warn("[DEV SEED] Nao foi possivel autenticar no Hub para semear dados de dev do checklist: {}", exception.getMessage());
                return;
            }

            seedTemplates(hubClient, selfClient, token);
            seedSubmissionWindows(hubClient, selfClient, token);
        };
    }

    // --- Templates de checklist (um ATIVO por sala do Hub) ---

    private void seedTemplates(RestClient hubClient, RestClient selfClient, String token) {
        List<HubRoomSummary> rooms;
        try {
            rooms = listRooms(hubClient, token);
        } catch (Exception exception) {
            log.warn("[DEV SEED] Nao foi possivel listar salas do Hub para semear templates de checklist: {}", exception.getMessage());
            return;
        }

        int seeded = 0;
        for (HubRoomSummary room : rooms) {
            if (hasActiveTemplate(selfClient, token, room.id())) {
                continue;
            }
            seedTemplateForRoom(selfClient, token, room);
            seeded++;
        }

        log.info(
            "[DEV SEED] Templates de checklist verificados para {} sala(s) do Hub ({} nova(s) criada(s) e ativada(s)).",
            rooms.size(),
            seeded
        );
    }

    private List<HubRoomSummary> listRooms(RestClient client, String token) {
        HubRoomSummary[] response = client.get()
            .uri("/rooms")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .body(HubRoomSummary[].class);

        return response == null ? List.of() : List.of(response);
    }

    private boolean hasActiveTemplate(RestClient client, String token, UUID roomId) {
        try {
            TemplateSummary[] templates = client.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/checklist-templates")
                    .queryParam("roomId", roomId)
                    .queryParam("status", "ACTIVE")
                    .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(TemplateSummary[].class);
            return templates != null && templates.length > 0;
        } catch (Exception exception) {
            log.warn("[DEV SEED] Falha ao consultar templates existentes da sala {}: {}", roomId, exception.getMessage());
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private void seedTemplateForRoom(RestClient client, String token, HubRoomSummary room) {
        try {
            String category = "LABORATORY".equals(room.typeRoom()) ? "ELETRONICOS" : "GERAL";
            String title = "Checklist padrao - Sala " + room.number();

            Map<String, Object> createBody = Map.of(
                "roomId", room.id(),
                "title", title,
                "description", "Template semeado automaticamente para o ambiente de dev.",
                "category", category,
                "schemaJson", Map.of("sections", List.of(defaultSection()))
            );

            Map<String, Object> created = client.post()
                .uri("/api/checklist-templates")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody)
                .retrieve()
                .body(Map.class);

            Object id = created == null ? null : created.get("id");
            if (id == null) {
                throw new IllegalStateException("Resposta de criacao de template sem id.");
            }

            client.patch()
                .uri("/api/checklist-templates/{id}/activate", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toBodilessEntity();

            log.info("[DEV SEED] Template de checklist semeado e ativado: sala {} ({}).", room.number(), title);
        } catch (Exception exception) {
            log.warn("[DEV SEED] Falha ao semear template da sala {}: {}", room.number(), exception.getMessage());
        }
    }

    private static Map<String, Object> defaultSection() {
        return Map.of(
            "key", "geral",
            "title", "Geral",
            "order", 1,
            "items", List.of(
                Map.of("key", "iluminacao", "title", "Iluminacao funcionando", "required", true, "order", 1),
                Map.of("key", "limpeza", "title", "Sala limpa e organizada", "required", true, "order", 2)
            )
        );
    }

    private void seedSubmissionWindows(RestClient hubClient, RestClient selfClient, String token) {
        List<HubClassSummary> classes;
        try {
            classes = listActiveClasses(hubClient, token);
        } catch (Exception exception) {
            log.warn("[DEV SEED] Nao foi possivel listar turmas do Hub para semear janelas de envio: {}", exception.getMessage());
            return;
        }

        for (String className : CLASS_NAMES_TO_SEED) {
            classes.stream()
                .filter(candidate -> candidate.name().equalsIgnoreCase(className))
                .findFirst()
                .ifPresentOrElse(
                    cls -> upsertArrivalWindow(selfClient, token, cls),
                    () -> log.warn("[DEV SEED] Turma {} nao encontrada no Hub; janela de envio nao semeada.", className)
                );
        }

        log.info(
            "[DEV SEED] Janelas de envio do checklist semeadas (ARRIVAL, 00:00-23:59): {}. "
                + "ADSIS2 fica sem janela de proposito, para testar o estado \"sem janela configurada\".",
            CLASS_NAMES_TO_SEED
        );
    }

    private List<HubClassSummary> listActiveClasses(RestClient client, String token) {
        HubListClassesResponse response = client.get()
            .uri("/classes?page=0&size=100")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .body(HubListClassesResponse.class);

        return response == null || response.items() == null ? List.of() : response.items();
    }

    private void upsertArrivalWindow(RestClient client, String token, HubClassSummary cls) {
        try {
            client.put()
                .uri("/api/submission-windows/classes/{classId}/ARRIVAL", cls.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("openAt", "00:00", "durationMinutes", 1439))
                .retrieve()
                .toBodilessEntity();
            log.info("[DEV SEED] Janela de envio semeada: {} (ARRIVAL, 00:00-23:59).", cls.name());
        } catch (Exception exception) {
            log.warn("[DEV SEED] Falha ao semear janela de envio da turma {}: {}", cls.name(), exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String login(RestClient client) {
        Map<String, Object> response = client.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("email", DEV_ADMIN_EMAIL, "password", DEV_ADMIN_PASSWORD))
            .retrieve()
            .body(Map.class);

        Object accessToken = response == null ? null : response.get("accessToken");
        if (!(accessToken instanceof String token) || token.isBlank()) {
            throw new IllegalStateException("Resposta de login do Hub sem accessToken.");
        }
        return token;
    }

    private record HubRoomSummary(UUID id, Integer number, String typeRoom) {
    }

    private record TemplateSummary(UUID id) {
    }

    private record HubClassSummary(UUID id, String name) {
    }

    private record HubListClassesResponse(List<HubClassSummary> items) {
    }
}
