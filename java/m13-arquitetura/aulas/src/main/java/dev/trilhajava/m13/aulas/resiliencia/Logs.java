package dev.trilhajava.m13.aulas.resiliencia;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Só para as demonstrações: sem o Spring Boot, o Logback mostraria o DEBUG de todas as bibliotecas. */
final class Logs {

    private Logs() { }

    static void soAvisosDasBibliotecas() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("dev.trilhajava")).setLevel(Level.INFO);
    }
}
