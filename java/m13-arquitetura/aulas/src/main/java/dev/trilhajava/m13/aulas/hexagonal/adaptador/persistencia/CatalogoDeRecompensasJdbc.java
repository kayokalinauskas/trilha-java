package dev.trilhajava.m13.aulas.hexagonal.adaptador.persistencia;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CatalogoDeRecompensas;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Pontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Recompensa;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class CatalogoDeRecompensasJdbc implements CatalogoDeRecompensas {

    private final JdbcClient jdbc;

    CatalogoDeRecompensasJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Recompensa> porCodigo(String codigo) {
        return jdbc.sql("select codigo, descricao, custo, ativa from recompensa where codigo = :codigo")
                .param("codigo", codigo)
                .query((linha, numero) -> new Recompensa(
                        linha.getString("codigo"),
                        linha.getString("descricao"),
                        Pontos.de(linha.getLong("custo")),
                        linha.getBoolean("ativa")))
                .optional();
    }
}
