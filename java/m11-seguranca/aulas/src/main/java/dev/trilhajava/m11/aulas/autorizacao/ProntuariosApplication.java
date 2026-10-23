package dev.trilhajava.m11.aulas.autorizacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aula 11.04: a API de prontuários de uma rede de clínicas (SaaS com várias clínicas, os "tenants").
 * Rode com a variável PRONTUARIOS_JWT_SEGREDO definida (32 bytes ou mais).
 */
@SpringBootApplication
public class ProntuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProntuariosApplication.class, args);
    }
}
