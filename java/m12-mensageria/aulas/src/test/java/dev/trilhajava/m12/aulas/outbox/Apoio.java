package dev.trilhajava.m12.aulas.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

/** Peças de teste: um H2 novo por teste, um broker em memória e um relógio que anda quando mandam. */
final class Apoio {

    private Apoio() {}

    record Banco(JdbcClient jdbc, TransactionTemplate transacao) {}

    static Banco bancoNovo() {
        var dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:loja-" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        new ResourceDatabasePopulator(new ClassPathResource("dev/trilhajava/m12/aulas/outbox/schema.sql"))
            .execute(dataSource);
        var jdbc = JdbcClient.create(dataSource);
        jdbc.sql("INSERT INTO produto (sku, estoque) VALUES ('CAFE-500G', 10)").update();
        return new Banco(jdbc, new TransactionTemplate(new DataSourceTransactionManager(dataSource)));
    }

    /** Guarda o que foi "publicado" e recusa as chaves que o teste mandar. */
    static final class BrokerEmMemoria implements Publicador {
        final List<Mensagem> recebidas = new ArrayList<>();
        final Set<String> chavesRecusadas = new HashSet<>();

        @Override
        public synchronized void publicar(Mensagem mensagem) {
            if (chavesRecusadas.contains(mensagem.chave())) {
                throw new IllegalStateException("broker indisponível para a partição de " + mensagem.chave());
            }
            recebidas.add(mensagem);
        }

        synchronized List<String> chaves() {
            return recebidas.stream().map(Mensagem::chave).toList();
        }
    }

    static final class Relogio extends Clock {
        private Instant agora = Instant.parse("2026-10-06T12:00:00Z");

        void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override public Instant instant() { return agora; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zona) { return this; }
    }
}
