package dev.trilhajava.m11.aulas.owasp.injecao;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/** Um H2 em memória por teste, com quatro contas em duas agências. */
final class BancoDeContas {

    private BancoDeContas() {
    }

    static EmbeddedDatabase novo() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("owasp/contas.sql")
                .build();
    }

    static JdbcClient cliente(EmbeddedDatabase banco) {
        return JdbcClient.create(banco);
    }
}
