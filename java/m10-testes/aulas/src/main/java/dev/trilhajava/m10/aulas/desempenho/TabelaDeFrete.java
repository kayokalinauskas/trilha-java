package dev.trilhajava.m10.aulas.desempenho;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Consulta a faixa do CEP no banco. A consulta em si é rápida (menos de 1 ms). */
@Repository
public class TabelaDeFrete {

    private final JdbcClient jdbc;

    public TabelaDeFrete(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<FaixaDeFrete> faixaDo(String cep) {
        return jdbc.sql("""
                        SELECT regiao, valor_base
                          FROM faixa_de_frete
                         WHERE :cep BETWEEN cep_inicial AND cep_final""")
                .param("cep", cep)
                .query(FaixaDeFrete.class)
                .optional();
    }
}
