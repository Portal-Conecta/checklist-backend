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
 * Popula janelas de envio (submission windows) para turmas do Hub no perfil
 * {@code dev}, evitando que o estado "sem janela configurada" apareca por
 * falta de dado de teste em vez de por comportamento real.
 *
 * <p>O checklist-backend nao possui as turmas em seu proprio banco (elas sao
 * do Hub), entao esse seeder autentica como o admin de dev ja semeado pelo
 * {@code core-backend} e usa a API HTTP (Hub para listar turmas, o proprio
 * servico via loopback para criar as janelas) em vez de chamar os casos de
 * uso diretamente -- eles exigem um contexto de requisicao autenticado que
 * nao existe durante o startup.</p>
 *
 * <p>Deixa a turma ADSIS2 de proposito sem janela, para o estado "sem janela
 * configurada" continuar testavel sem precisar desfazer nada manualmente.
 * Falhas aqui (Hub fora do ar, admin nao encontrado) sao logadas como aviso e
 * nao impedem a subida do servico -- e conveniencia de dev, nao requisito.</p>
 *
 * <p><strong>Ativo apenas no perfil {@code dev}. Nao deve ser executado em
 * producao.</strong></p>
 */
@Configuration
@Profile("dev")
public class ChecklistSubmissionWindowDevSeedInitializer {

    private static final Logger log = LoggerFactory.getLogger(ChecklistSubmissionWindowDevSeedInitializer.class);

    private static final String DEV_ADMIN_EMAIL = "admin@portal.test";
    private static final String DEV_ADMIN_PASSWORD = "123456";

    // ARRIVAL, dia todo. ADSIS2 fica de fora de proposito -- ver javadoc da classe.
    private static final List<String> CLASS_NAMES_TO_SEED = List.of("MIDS1", "ADSIS1", "MIDS2");

    @Bean
    public CommandLineRunner seedChecklistSubmissionWindows(
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
                log.warn("[DEV SEED] Nao foi possivel autenticar no Hub para semear janelas de envio: {}", exception.getMessage());
                return;
            }

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
        };
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

    private record HubClassSummary(UUID id, String name) {
    }

    private record HubListClassesResponse(List<HubClassSummary> items) {
    }
}
