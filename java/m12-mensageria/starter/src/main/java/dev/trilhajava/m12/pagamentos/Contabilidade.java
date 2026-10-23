package dev.trilhajava.m12.pagamentos;

import java.math.BigDecimal;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Lança o valor no livro contábil. Grava no MESMO banco: participa da transação de quem chama. */
@Component
public class Contabilidade {

    private final JdbcClient jdbc;

    public Contabilidade(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void lancar(String pedidoId, BigDecimal valor, String historico) {
        jdbc.sql("INSERT INTO lancamento_contabil (pedido_id, valor, historico) VALUES (?, ?, ?)")
                .params(pedidoId, valor, historico)
                .update();
    }
}
