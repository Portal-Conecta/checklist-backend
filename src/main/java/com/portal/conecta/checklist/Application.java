package com.portal.conecta.checklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Ponto de entrada da Checklist API.
 *
 * <p>Variaveis locais do arquivo {@code .env} sao carregadas automaticamente
 * pelo {@code DotEnvEnvironmentPostProcessor} antes do contexto Spring
 * iniciar, permitindo executar o projeto em ambientes locais sem depender de
 * configuracao global da maquina.</p>
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.portal.conecta.checklist.shared.integration.hub.client")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
