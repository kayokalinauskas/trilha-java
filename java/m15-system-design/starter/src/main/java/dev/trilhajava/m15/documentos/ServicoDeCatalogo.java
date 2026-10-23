package dev.trilhajava.m15.documentos;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServicoDeCatalogo {

    public static final int AVALIACOES_EMBUTIDAS = 10;

    public record ResumoDaCategoria(String categoria, long produtos, BigDecimal precoMedio) {
    }

    private final MongoTemplate mongo;
    private final RepositorioDeProdutos produtos;

    public ServicoDeCatalogo(MongoTemplate mongo, RepositorioDeProdutos produtos) {
        this.mongo = mongo;
        this.produtos = produtos;
    }

    /** Faixa FECHADA: minimo e maximo entram no resultado. */
    public Page<Produto> porFaixaDePreco(String categoria, BigDecimal minimo, BigDecimal maximo, Pageable pagina) {
        throw new UnsupportedOperationException("TODO: exercício 15.02");
    }

    /** Produtos com atributos.{nome} = valor, por exemplo atributos.cor = "azul". */
    public List<Produto> comAtributo(String nome, String valor) {
        throw new UnsupportedOperationException("TODO: exercício 15.02");
    }

    /** Baixa o estoque só se houver o suficiente, sem vender a mesma unidade duas vezes. */
    public boolean reservar(String sku, int quantidade) {
        throw new UnsupportedOperationException("TODO: exercício 15.02");
    }

    /** Guarda só as 10 avaliações mais recentes no documento, mas conta todas. */
    public void avaliar(String sku, Avaliacao avaliacao) {
        throw new UnsupportedOperationException("TODO: exercício 15.02");
    }

    /** Quantidade de produtos e preço médio (2 casas) por categoria, em ordem de categoria. */
    public List<ResumoDaCategoria> resumoPorCategoria() {
        throw new UnsupportedOperationException("TODO: exercício 15.02");
    }
}
