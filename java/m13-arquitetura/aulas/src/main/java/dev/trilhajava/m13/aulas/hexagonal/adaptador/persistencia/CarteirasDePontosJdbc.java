package dev.trilhajava.m13.aulas.hexagonal.adaptador.persistencia;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CarteiraAlteradaException;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CarteirasDePontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.CarteiraDePontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Pontos;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Adaptador de saída: implementa a porta com SQL. É aqui, e só aqui, que linha vira objeto de domínio. */
@Repository
class CarteirasDePontosJdbc implements CarteirasDePontos {

    private final JdbcClient jdbc;

    CarteirasDePontosJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CarteiraDePontos> doCliente(String clienteId) {
        return jdbc.sql("select cliente_id, saldo, versao from carteira_de_pontos where cliente_id = :id")
                .param("id", clienteId)
                .query((linha, numero) -> CarteiraDePontos.reconstituir(
                        linha.getString("cliente_id"),
                        Pontos.de(linha.getLong("saldo")),
                        linha.getLong("versao")))
                .optional();
    }

    @Override
    public void salvar(CarteiraDePontos carteira) {
        int alteradas = jdbc.sql("""
                        update carteira_de_pontos
                           set saldo = :saldo, versao = versao + 1
                         where cliente_id = :id and versao = :versao""")
                .param("saldo", carteira.saldo().quantidade())
                .param("id", carteira.clienteId())
                .param("versao", carteira.versao())
                .update();
        if (alteradas == 0) {
            throw new CarteiraAlteradaException(carteira.clienteId());
        }
    }
}
