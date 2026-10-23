package dev.trilhajava.m15.documentos;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ServicoDeCatalogo {

    public static final int AVALIACOES_EMBUTIDAS = 10;

    /** Nome de atributo vira parte do caminho do campo: nada de "$", "." ou espaços vindos do usuário. */
    private static final Pattern NOME_DE_ATRIBUTO = Pattern.compile("[a-z][a-zA-Z0-9]{0,30}");

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
        return produtos.findByCategoriaAndPrecoBetween(categoria, Range.closed(minimo, maximo), pagina);
    }

    /** Produtos com atributos.{nome} = valor, por exemplo atributos.cor = "azul". */
    public List<Produto> comAtributo(String nome, String valor) {
        if (!NOME_DE_ATRIBUTO.matcher(nome).matches()) {
            throw new IllegalArgumentException("Nome de atributo inválido: " + nome);
        }
        return mongo.find(query(where("atributos." + nome).is(valor)).with(Sort.by("sku")), Produto.class);
    }

    /**
     * Baixa o estoque só se houver o suficiente, numa única operação atômica no servidor. Ler, conferir
     * em Java e gravar seriam três passos, e duas requisições simultâneas venderiam a mesma unidade.
     */
    public boolean reservar(String sku, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("quantidade deve ser positiva");
        }
        var resultado = mongo.updateFirst(
                query(where("sku").is(sku).and("estoque").gte(quantidade)),
                new Update().inc("estoque", -quantidade),
                Produto.class);
        return resultado.getModifiedCount() == 1;
    }

    /** Guarda só as 10 avaliações mais recentes no documento, mas conta todas. Uma operação só. */
    public void avaliar(String sku, Avaliacao avaliacao) {
        var update = new Update()
                .push("avaliacoes").slice(-AVALIACOES_EMBUTIDAS).each(avaliacao)
                .inc("totalAvaliacoes", 1)
                .inc("somaDasNotas", avaliacao.nota());
        var resultado = mongo.updateFirst(query(where("sku").is(sku)), update, Produto.class);
        if (resultado.getMatchedCount() == 0) {
            throw new IllegalArgumentException("Produto não encontrado: " + sku);
        }
    }

    /** Quantidade de produtos e preço médio por categoria, calculados no servidor (aggregation pipeline). */
    public List<ResumoDaCategoria> resumoPorCategoria() {
        var pipeline = newAggregation(Produto.class,
                group("categoria").count().as("produtos").avg("preco").as("precoMedio"),
                project("produtos", "precoMedio").and("_id").as("categoria"),
                sort(Sort.by("categoria")));
        return mongo.aggregate(pipeline, ResumoDaCategoria.class).getMappedResults().stream()
                .map(r -> new ResumoDaCategoria(r.categoria(), r.produtos(), r.precoMedio().setScale(2, RoundingMode.HALF_EVEN)))
                .toList();
    }
}
