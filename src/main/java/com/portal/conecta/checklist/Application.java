package com.portal.conecta.checklist;

import com.portal.conecta.checklist.shared.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da Checklist API.
 *
 * <p>Carrega variaveis locais do arquivo {@code .env} antes de iniciar o
 * contexto Spring, permitindo executar o projeto em ambientes locais sem
 * depender de configuracao global da maquina.</p>
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		EnvFileLoader.loadFromWorkingDirectory();
		SpringApplication.run(Application.class, args);
	}

}
