package dev.trilhajava.m12.aulas.idempotencia;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

/** Um H2 em memória, novo a cada teste, com o esquema da aula. Sem Spring Boot e sem Docker. */
final class BancoDeTeste {

    static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-06T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    final DataSource dataSource;
    final JdbcClient jdbc;
    final TransactionTemplate transacao;

    BancoDeTeste() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        this.dataSource = new DriverManagerDataSource(url, "sa", "");
        new ResourceDatabasePopulator(new ClassPathResource("idempotencia/schema.sql")).execute(dataSource);
        this.jdbc = JdbcClient.create(dataSource);
        this.transacao = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }
}
