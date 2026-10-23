package dev.trilhajava.m12.aulas.rabbitmq;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * Aplicação da aula 12.02. Precisa de um RabbitMQ em localhost:5672 (veja a aula).
 * Com clinica.demo.publicar=true, publica quatro exames na subida.
 */
@SpringBootApplication
@PropertySource("classpath:aulas-rabbitmq.properties")
public class ClinicaApplication {

    private static final Logger log = LoggerFactory.getLogger(ClinicaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ClinicaApplication.class, args);
    }

    /** Implementação de demonstração: "gera" o laudo em 2 segundos. */
    @Bean
    GeradorDeLaudo geradorDeLaudo() {
        return exame -> {
            if (exame.tipo() != TipoDeExame.IMAGEM) {
                throw new LaudoInvalidoException("exame de " + exame.tipo() + " não tem laudo de imagem");
            }
            try {
                Thread.sleep(Duration.ofSeconds(2));    // simula a renderização do PDF
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("geração de laudo interrompida", e);
            }
        };
    }

    /** Implementação de demonstração: o paciente "PAC-SEM-CONTATO" não tem como ser avisado. */
    @Bean
    NotificadorDePaciente notificadorDePaciente() {
        return exame -> {
            if (exame.pacienteId().equals("PAC-SEM-CONTATO")) {
                throw new PacienteSemContatoException(exame.pacienteId());
            }
            log.info("Aviso enviado: paciente={} exame={}", exame.pacienteId(), exame.tipo());
        };
    }

    @Bean
    @ConditionalOnProperty(name = "clinica.demo.publicar", havingValue = "true")
    ApplicationRunner publicarExamesDeDemonstracao(PublicadorDeExames publicador) {
        return argumentos -> {
            Instant agora = Instant.now();
            var exames = new ExameConcluido[] {
                ExameConcluido.novo("PAC-1001", TipoDeExame.IMAGEM, "sp", agora),
                ExameConcluido.novo("PAC-1002", TipoDeExame.SANGUE, "sp", agora),
                ExameConcluido.novo("PAC-1003", TipoDeExame.IMAGEM, "rj", agora),
                ExameConcluido.novo("PAC-SEM-CONTATO", TipoDeExame.URINA, "rj", agora),
            };
            for (ExameConcluido exame : exames) {
                publicador.publicarComConfirmacao(exame, Duration.ofSeconds(5));
                log.info("Publicado e confirmado: {} ({})", exame.chaveDeRoteamento(), exame.pacienteId());
            }
        };
    }
}
