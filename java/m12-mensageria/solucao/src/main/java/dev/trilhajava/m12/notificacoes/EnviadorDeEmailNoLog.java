package dev.trilhajava.m12.notificacoes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Implementação de desenvolvimento: só registra no log. Os testes a substituem por um mock. */
@Component
class EnviadorDeEmailNoLog implements EnviadorDeEmail {

    private static final Logger log = LoggerFactory.getLogger(EnviadorDeEmailNoLog.class);

    @Override
    public void enviar(String destinatario, String mensagem) {
        log.info("e-mail para {}: {}", destinatario, mensagem);
    }
}
