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
 * ponta, no perfil {@code dev}: um template ATIVO por sala fixa do seed do
 * Hub e janelas de envio (submission windows) pras turmas de teste.
 *
 * <p>O checklist-backend nao possui turmas nem salas em seu proprio banco
 * (sao do Hub), entao este seeder autentica como o admin de dev ja semeado
 * pelo {@code core-backend} e usa a API HTTP (o proprio servico via loopback
 * pra criar templates e janelas) em vez de
 * chamar os casos de uso diretamente -- eles exigem um contexto de requisicao
 * autenticado que nao existe durante o startup.</p>
 *
 * <p>Deixa a turma MT77 de proposito sem janela, para o estado "sem janela
 * configurada" continuar testavel sem precisar desfazer nada manualmente.
 * Pelo mesmo motivo, deixa a sala 214 de proposito sem template ATIVO, para
 * o estado "sala sem checklist" tambem continuar testavel. Templates sao
 * idempotentes (pula a sala se ja existir template ATIVO).
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

    /**
     * Referencias do seed de desenvolvimento do Hub. Nao derive estes IDs da
     * resposta HTTP: um banco local antigo pode conter a mesma sala/turma com
     * outro UUID e criar referencias invalidas no Checklist.
     */
    private static final List<DevRoomSeed> DEV_ROOMS = List.of(
        new DevRoomSeed("00000000-0000-0000-0000-000000000101", 101, "LABORATORY"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000102", 102, "LABORATORY"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000103", 103, "LABORATORY"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000109", 109, "LABORATORY"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000110", 110, "LABORATORY"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000201", 201, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000202", 202, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000203", 203, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000204", 204, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000205", 205, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000206", 206, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000207", 207, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000212", 212, "CLASSROOM"),
        new DevRoomSeed("00000000-0000-0000-0000-000000000213", 213, "CLASSROOM")
    );

    /** Turmas com janela ARRIVAL; IDs alinhados ao {@code DevDataInitializer} do core-backend. */
    private static final List<DevClassSeed> CLASSES_WITH_ARRIVAL_WINDOW = List.of(
        new DevClassSeed("00000000-0000-0000-0000-000000000101", "MI78"),
        new DevClassSeed("00000000-0000-0000-0000-000000000102", "MI77"),
        new DevClassSeed("00000000-0000-0000-0000-000000000103", "MT78")
    );

    /** Turma deixada de proposito sem janela de envio, para testar o estado "sem janela configurada". */
    private static final String CLASS_NAME_WITHOUT_WINDOW = "MT77";

    /** Sala deixada de proposito sem template ATIVO, para testar o estado "sala sem checklist". */
    private static final int ROOM_NUMBER_WITHOUT_TEMPLATE = 214;

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

            seedTemplates(selfClient, token);
            seedSubmissionWindows(selfClient, token);
        };
    }

    // --- Templates de checklist (um ATIVO por sala do Hub) ---

    private void seedTemplates(RestClient selfClient, String token) {
        int seeded = 0;
        for (DevRoomSeed room : DEV_ROOMS) {
            if (hasActiveTemplate(selfClient, token, room.id())) {
                continue;
            }
            seedTemplateForRoom(selfClient, token, room);
            seeded++;
        }

        log.info(
            "[DEV SEED] Templates de checklist verificados para {} sala(s) fixa(s) do Hub ({} nova(s) criada(s) e ativada(s)). "
                + "Sala {} fica sem template de proposito, para testar o estado \"sala sem checklist\".",
            DEV_ROOMS.size(),
            seeded,
            ROOM_NUMBER_WITHOUT_TEMPLATE
        );
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
    private void seedTemplateForRoom(RestClient client, String token, DevRoomSeed room) {
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

    private void seedSubmissionWindows(RestClient selfClient, String token) {
        CLASSES_WITH_ARRIVAL_WINDOW.forEach(cls -> upsertArrivalWindow(selfClient, token, cls));

        log.info(
            "[DEV SEED] Janelas de envio do checklist semeadas (ARRIVAL, 00:00-23:59): {}. "
                + "Turma {} fica sem janela de proposito, para testar o estado \"sem janela configurada\".",
            CLASSES_WITH_ARRIVAL_WINDOW.stream().map(DevClassSeed::name).toList(),
            CLASS_NAME_WITHOUT_WINDOW
        );
    }

    private void upsertArrivalWindow(RestClient client, String token, DevClassSeed cls) {
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

    private record TemplateSummary(UUID id) {
    }

    private record DevRoomSeed(UUID id, int number, String typeRoom) {
        private DevRoomSeed(String id, int number, String typeRoom) {
            this(UUID.fromString(id), number, typeRoom);
        }
    }

    private record DevClassSeed(UUID id, String name) {
        private DevClassSeed(String id, String name) {
            this(UUID.fromString(id), name);
        }
    }
}
