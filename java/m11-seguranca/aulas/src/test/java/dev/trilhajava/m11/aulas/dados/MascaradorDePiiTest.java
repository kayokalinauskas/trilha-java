package dev.trilhajava.m11.aulas.dados;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class MascaradorDePiiTest {

    @Test
    void mascaraOsCamposConhecidos() {
        assertThat(MascaradorDePii.cpf("12345678909")).isEqualTo("***.***.***-09");
        assertThat(MascaradorDePii.email("marina.souza@clinica.com.br")).isEqualTo("m***@clinica.com.br");
        assertThat(MascaradorDePii.telefone("+55 11 98765-4321")).isEqualTo("(**) *****-4321");
    }

    @Test
    void oToStringDoCpfJaSaiMascarado() {
        Cpf cpf = new Cpf("123.456.789-09");

        assertThat(cpf.toString()).isEqualTo("***.***.***-09");
        assertThat("cadastro de " + cpf).isEqualTo("cadastro de ***.***.***-09");
        assertThat(cpf.digitos()).isEqualTo("12345678909");   // só quem pede explicitamente
    }

    @Test
    void redeDeSegurancaParaTextoLivre() {
        String mensagem = "falha ao validar 123.456.789-09 e 98765432100 no pedido 202612345678";

        assertThat(MascaradorDePii.mascararCpfsNoTexto(mensagem))
                .isEqualTo("falha ao validar ***.***.***-09 e ***.***.***-00 no pedido 202612345678");
    }

    @Test
    void oLogDoLoggerRecebeOCpfMascarado() {
        Logger logger = (Logger) LoggerFactory.getLogger(CadastroDePacientes.class);
        ListAppender<ILoggingEvent> capturados = new ListAppender<>();
        capturados.start();
        Level nivelAnterior = logger.getLevel();
        logger.setLevel(Level.INFO);   // a suíte roda com WARN; aqui queremos ver o INFO
        logger.addAppender(capturados);
        try {
            Cpf cpf = new Cpf("12345678909");
            logger.info("paciente cadastrado: id={} cpf={}", 42, cpf);
        } finally {
            logger.detachAppender(capturados);
            logger.setLevel(nivelAnterior);
        }

        assertThat(capturados.list).singleElement()
                .extracting(ILoggingEvent::getFormattedMessage)
                .isEqualTo("paciente cadastrado: id=42 cpf=***.***.***-09");
    }
}
