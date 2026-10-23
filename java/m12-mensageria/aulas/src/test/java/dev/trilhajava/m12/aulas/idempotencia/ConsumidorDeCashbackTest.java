package dev.trilhajava.m12.aulas.idempotencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("Consumidor de cashback")
class ConsumidorDeCashbackTest {

    BancoDeTeste banco;
    List<String> avisos;
    ConsumidorDeCashback consumidor;

    @BeforeEach
    void prepara() {
        banco = new BancoDeTeste();
        avisos = new ArrayList<>();
        Notificador notificador = (conta, valor) -> avisos.add(conta + " +" + valor);
        consumidor = new ConsumidorDeCashback(banco.jdbc, banco.transacao, notificador, BancoDeTeste.RELOGIO);
    }

    @Test
    @DisplayName("credita uma vez só: reentrega e mesma compra com outro eventoId são duplicatas")
    void duplicatasNaoCreditamDeNovo() {
        criarConta("CT-ANA", "100.00");
        var evento = new CashbackConcedido("EVT-1", "CT-ANA", "COMPRA-77", new BigDecimal("12.50"));
        var republicado = new CashbackConcedido("EVT-9", "CT-ANA", "COMPRA-77", new BigDecimal("12.50"));

        assertThat(consumidor.processar(evento)).isEqualTo(Resultado.PROCESSADO);
        assertThat(consumidor.processar(evento)).isEqualTo(Resultado.DUPLICADO);
        assertThat(consumidor.processar(republicado)).isEqualTo(Resultado.DUPLICADO);

        assertThat(saldo("CT-ANA")).isEqualByComparingTo("112.50");
        assertThat(avisos).containsExactly("CT-ANA +12.50");
    }

    @Test
    @DisplayName("falha no meio desfaz a marca, e a reentrega processa normalmente")
    void falhaDesfazTudo() {
        var evento = new CashbackConcedido("EVT-2", "CT-BRUNO", "COMPRA-78", new BigDecimal("8.00"));

        assertThatThrownBy(() -> consumidor.processar(evento))
            .isInstanceOf(DataIntegrityViolationException.class);   // a conta CT-BRUNO ainda não existe
        assertThat(contar("mensagem_processada")).isZero();
        assertThat(contar("lancamento")).isZero();
        assertThat(avisos).isEmpty();

        criarConta("CT-BRUNO", "0.00");
        assertThat(consumidor.processar(evento)).isEqualTo(Resultado.PROCESSADO);
        assertThat(saldo("CT-BRUNO")).isEqualByComparingTo("8.00");
    }

    private void criarConta(String id, String saldo) {
        banco.jdbc.sql("INSERT INTO conta (id, saldo) VALUES (?, ?)").params(id, new BigDecimal(saldo)).update();
    }

    private BigDecimal saldo(String conta) {
        return banco.jdbc.sql("SELECT saldo FROM conta WHERE id = ?").param(conta).query(BigDecimal.class).single();
    }

    private long contar(String tabela) {
        return banco.jdbc.sql("SELECT COUNT(*) FROM " + tabela).query(Long.class).single();
    }
}
