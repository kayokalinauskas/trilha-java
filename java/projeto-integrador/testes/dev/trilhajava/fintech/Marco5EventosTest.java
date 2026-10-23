package dev.trilhajava.fintech;

import static dev.trilhajava.fintech.ApiDeTeste.cpf;
import static dev.trilhajava.fintech.ApiDeTeste.novaConta;
import static dev.trilhajava.fintech.ApiDeTeste.valor;
import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.trilhajava.fintech.ApiDeTeste.Resposta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Marco 5: a transferência grava um evento na outbox (na mesma transação), um relay o publica no
 * Kafka e um consumidor idempotente gera notificações. Roda com PostgreSQL e Kafka de verdade.
 *
 * O contrato verificado aqui (tabelas e JSON) está no enunciado do marco 5.
 */
@TesteDeAceitacao
@Import({PostgresDeTeste.class, KafkaDeTeste.class})
@TestPropertySource(properties = "fintech.eventos.habilitados=true")
@EnabledIfDockerAvailable
@DisplayName("Fintech Core — Marco 5: eventos com outbox, Kafka e consumidor idempotente")
class Marco5EventosTest {

    static final Duration ESPERA = Duration.ofSeconds(20);

    @LocalServerPort
    int porta;

    @Autowired
    DataSource dataSource;

    ApiDeTeste anonimo;

    @BeforeEach
    void cliente() {
        anonimo = ApiDeTeste.anonimo(porta);
    }

    // ------------------------------------------------------------------ auxiliares

    record Pessoa(String id, ApiDeTeste api) {
    }

    record Evento(String id, String tipo, String agregado, String payload, boolean publicado) {
        DocumentContext json() {
            return JsonPath.parse(payload);
        }
    }

    Pessoa pessoa(String nome) {
        String email = nome.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8) + "@exemplo.com";
        String senha = "senha-forte-de-" + nome.toLowerCase();
        Resposta cadastro = anonimo.post("/usuarios",
                "{\"nome\": \"%s\", \"email\": \"%s\", \"senha\": \"%s\"}".formatted(nome, email, senha));
        assertThat(cadastro.status()).as(cadastro.toString()).isEqualTo(201);
        Resposta token = anonimo.post("/auth/token", "{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha));
        assertThat(token.status()).as(token.toString()).isEqualTo(200);
        return new Pessoa(cadastro.ler("$.id", String.class), anonimo.comToken(token.ler("$.accessToken", String.class)));
    }

    String conta(Pessoa dono, String saldo) {
        Resposta r = dono.api().post("/contas", novaConta("Titular de teste", cpf(), "CORRENTE"));
        assertThat(r.status()).as(r.toString()).isEqualTo(201);
        String id = r.ler("$.id", String.class);
        if (saldo != null) {
            Resposta d = dono.api().post("/contas/" + id + "/depositos", valor(saldo));
            assertThat(d.status()).as(d.toString()).isEqualTo(200);
        }
        return id;
    }

    Resposta transferir(Pessoa quem, String chave, String origem, String destino, String quantia) {
        return quem.api().post("/transferencias",
                "{\"origem\": \"%s\", \"destino\": \"%s\", \"valor\": %s}".formatted(origem, destino, quantia),
                Map.of("Idempotency-Key", chave));
    }

    String transferirOk(Pessoa quem, String origem, String destino, String quantia) {
        Resposta r = transferir(quem, UUID.randomUUID().toString(), origem, destino, quantia);
        assertThat(r.status()).as(r.toString()).isEqualTo(201);
        return r.ler("$.id", String.class);
    }

    /** Os eventos da outbox cujo agregado é a conta informada, do mais antigo ao mais novo. */
    List<Evento> eventosDaConta(String conta) {
        List<Evento> eventos = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement("""
                        SELECT id, tipo, agregado_id, payload, publicado_em FROM evento_saida
                        WHERE agregado_id = ? ORDER BY criado_em""")) {
            ps.setObject(1, UUID.fromString(conta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    eventos.add(new Evento(rs.getString("id"), rs.getString("tipo"), rs.getString("agregado_id"),
                            rs.getString("payload"), rs.getTimestamp("publicado_em") != null));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return eventos;
    }

    void executarSql(String sql, Object... parametros) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    List<Map<String, Object>> notificacoes(Pessoa pessoa) {
        Resposta r = pessoa.api().get("/notificacoes");
        assertThat(r.status()).as(r.toString()).isEqualTo(200);
        return r.json().read("$.itens");
    }

    List<Map<String, Object>> notificacoesDa(Pessoa pessoa, String transferencia) {
        return notificacoes(pessoa).stream().filter(n -> transferencia.equals(n.get("transferencia"))).toList();
    }

    static void esperarAte(BooleanSupplier condicao, String descricao) {
        long limite = System.nanoTime() + ESPERA.toNanos();
        while (!condicao.getAsBoolean()) {
            if (System.nanoTime() > limite) {
                throw new AssertionError("não aconteceu em " + ESPERA.toSeconds() + " s: " + descricao);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }

    // ------------------------------------------------------------------ testes

    @Test
    @DisplayName("a transferência grava TransferenciaRealizada na outbox, com o contrato do JSON")
    void outbox() {
        Pessoa ana = pessoa("Ana");
        String origem = conta(ana, "500.00");
        String destino = conta(pessoa("Bia"), null);

        String transferencia = transferirOk(ana, origem, destino, "150.00");

        List<Evento> eventos = eventosDaConta(origem);
        assertThat(eventos).hasSize(1);
        Evento e = eventos.getFirst();
        assertThat(e.tipo()).isEqualTo("TransferenciaRealizada");
        assertThat(e.agregado()).isEqualTo(origem);
        DocumentContext json = e.json();
        assertThat(json.read("$.id", String.class)).isEqualTo(e.id());
        assertThat(json.read("$.tipo", String.class)).isEqualTo("TransferenciaRealizada");
        assertThat(json.read("$.transferencia", String.class)).isEqualTo(transferencia);
        assertThat(json.read("$.origem", String.class)).isEqualTo(origem);
        assertThat(json.read("$.destino", String.class)).isEqualTo(destino);
        assertThat(json.read("$.valor", String.class)).isEqualTo("150.00");
        assertThat(json.read("$.ocorridoEm", String.class)).isNotBlank();
    }

    @Test
    @DisplayName("transferência recusada (saldo insuficiente) não gera evento")
    void semEventoSemTransferencia() {
        Pessoa ana = pessoa("Ana");
        String origem = conta(ana, "10.00");
        String destino = conta(pessoa("Bia"), null);

        Resposta r = transferir(ana, UUID.randomUUID().toString(), origem, destino, "10.01");

        assertThat(r.status()).isEqualTo(422);
        assertThat(eventosDaConta(origem)).isEmpty();
    }

    @Test
    @DisplayName("repetir com a mesma Idempotency-Key não gera um segundo evento")
    void repeticaoSemEvento() {
        Pessoa ana = pessoa("Ana");
        String origem = conta(ana, "100.00");
        String destino = conta(pessoa("Bia"), null);
        String chave = UUID.randomUUID().toString();

        assertThat(transferir(ana, chave, origem, destino, "30.00").status()).isEqualTo(201);
        assertThat(transferir(ana, chave, origem, destino, "30.00").status()).isEqualTo(201);

        assertThat(eventosDaConta(origem)).hasSize(1);
    }

    @Test
    @DisplayName("o relay publica o evento: publicado_em é preenchido em poucos segundos")
    void relayPublica() {
        Pessoa ana = pessoa("Ana");
        String origem = conta(ana, "100.00");
        transferirOk(ana, origem, conta(pessoa("Bia"), null), "1.00");

        esperarAte(() -> eventosDaConta(origem).stream().allMatch(Evento::publicado), "o evento ser publicado");
    }

    @Test
    @DisplayName("quem enviou e quem recebeu são notificados; uma terceira pessoa, não")
    void notificacoes() {
        Pessoa ana = pessoa("Ana");
        Pessoa bia = pessoa("Bia");
        Pessoa caio = pessoa("Caio");
        String origem = conta(ana, "300.00");
        String destino = conta(bia, null);

        String transferencia = transferirOk(ana, origem, destino, "120.50");

        esperarAte(() -> notificacoesDa(bia, transferencia).size() == 1, "a Bia ser notificada");
        esperarAte(() -> notificacoesDa(ana, transferencia).size() == 1, "a Ana ser notificada");
        Map<String, Object> enviada = notificacoesDa(ana, transferencia).getFirst();
        Map<String, Object> recebida = notificacoesDa(bia, transferencia).getFirst();
        assertThat(enviada.get("tipo")).isEqualTo("TRANSFERENCIA_ENVIADA");
        assertThat(enviada.get("conta")).isEqualTo(origem);
        assertThat(recebida.get("tipo")).isEqualTo("TRANSFERENCIA_RECEBIDA");
        assertThat(recebida.get("conta")).isEqualTo(destino);
        assertThat(new java.math.BigDecimal(recebida.get("valor").toString())).isEqualByComparingTo("120.50");
        assertThat(recebida.get("mensagem").toString()).isNotBlank();
        assertThat(notificacoes(caio)).isEmpty();
    }

    @Test
    @DisplayName("as notificações vêm da mais recente para a mais antiga")
    void ordem() {
        Pessoa ana = pessoa("Ana");
        String origem = conta(ana, "100.00");
        String destino = conta(pessoa("Bia"), null);
        String primeira = transferirOk(ana, origem, destino, "1.00");
        String segunda = transferirOk(ana, origem, destino, "2.00");

        esperarAte(() -> notificacoes(ana).size() == 2, "as duas notificações da Ana");
        assertThat(notificacoes(ana)).extracting(n -> n.get("transferencia")).containsExactly(segunda, primeira);
    }

    @Test
    @DisplayName("reentrega: o mesmo evento publicado de novo NÃO duplica as notificações")
    void reentrega() {
        Pessoa ana = pessoa("Ana");
        Pessoa bia = pessoa("Bia");
        String origem = conta(ana, "100.00");
        String destino = conta(bia, null);
        String primeira = transferirOk(ana, origem, destino, "10.00");
        esperarAte(() -> notificacoesDa(bia, primeira).size() == 1, "a primeira notificação");

        // Simula o relay caindo depois de enviar e antes de marcar: o evento volta a ficar pendente.
        executarSql("UPDATE evento_saida SET publicado_em = NULL WHERE agregado_id = ?", UUID.fromString(origem));
        esperarAte(() -> eventosDaConta(origem).stream().allMatch(Evento::publicado), "o evento ser republicado");

        // Uma nova transferência da MESMA conta vai para a mesma partição: quando ela for consumida,
        // a reentrega (que veio antes, na mesma partição) já foi processada.
        String segunda = transferirOk(ana, origem, destino, "20.00");
        esperarAte(() -> notificacoesDa(bia, segunda).size() == 1, "a notificação da segunda transferência");

        assertThat(notificacoesDa(bia, primeira)).hasSize(1);
        assertThat(notificacoesDa(ana, primeira)).hasSize(1);
    }

    @Test
    @DisplayName("GET /notificacoes exige autenticação (401)")
    void exigeToken() {
        assertThat(anonimo.get("/notificacoes").status()).isEqualTo(401);
    }
}
