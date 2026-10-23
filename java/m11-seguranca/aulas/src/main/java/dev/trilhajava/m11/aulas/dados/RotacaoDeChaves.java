package dev.trilhajava.m11.aulas.dados;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regrava, em lotes, os CPFs cifrados com uma versão de chave antiga. Trabalha direto na coluna
 * (sem carregar entidades): lê {@code v1:...}, decifra com a v1 e grava {@code v2:...}.
 * O {@code WHERE cpf_cifrado = :antigo} evita sobrescrever um valor que mudou no meio do caminho.
 * Chame em laço até devolver zero; cada lote é uma transação curta.
 */
@Component
public class RotacaoDeChaves {

    private record Linha(long id, String cpfCifrado) {}

    private final JdbcClient jdbc;
    private final CifradorDeCampo cifrador;

    public RotacaoDeChaves(DataSource dataSource, CifradorDeCampo cifrador) {
        this.jdbc = JdbcClient.create(dataSource);
        this.cifrador = cifrador;
    }

    @Transactional
    public int recifrarLote(int tamanhoDoLote) {
        List<Linha> pendentes = jdbc.sql("""
                        SELECT id, cpf_cifrado FROM paciente
                        WHERE cpf_cifrado NOT LIKE :prefixo
                        ORDER BY id
                        FETCH FIRST :lote ROWS ONLY""")
                .param("prefixo", cifrador.prefixoAtual() + "%")
                .param("lote", tamanhoDoLote)
                .query(Linha.class)
                .list();
        int regravadas = 0;
        for (Linha linha : pendentes) {
            String novo = cifrador.recifrar(linha.cpfCifrado(), ConversorDeCpfCifrado.CONTEXTO);
            regravadas += jdbc.sql("UPDATE paciente SET cpf_cifrado = :novo WHERE id = :id AND cpf_cifrado = :antigo")
                    .param("novo", novo)
                    .param("id", linha.id())
                    .param("antigo", linha.cpfCifrado())
                    .update();
        }
        return regravadas;
    }
}
