package dev.trilhajava.m15.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m15.documentos.ServicoDeCatalogo.ResumoDaCategoria;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest(classes = DocumentosApplication.class)
@Testcontainers
@EnabledIfDockerAvailable
@DisplayName("Exercício 15.02 — catálogo de produtos no MongoDB")
class CatalogoMongoTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8");

    @Autowired
    RepositorioDeProdutos produtos;

    @Autowired
    ServicoDeCatalogo catalogo;

    @Autowired
    MongoTemplate mongo;

    @BeforeEach
    void limpar() {
        produtos.deleteAll();
    }

    Produto salvar(String sku, String categoria, String preco, int estoque, Map<String, String> atributos) {
        return produtos.save(Produto.novo(sku, "Produto " + sku, categoria, preco, estoque, atributos));
    }

    @Test
    @DisplayName("documentos da mesma coleção têm atributos diferentes, sem esquema fixo")
    void atributosFlexiveis() {
        salvar("CAM-1", "roupas", "59.90", 5, Map.of("cor", "azul", "tamanho", "M"));
        salvar("NOT-1", "informatica", "4999.00", 2, Map.of("memoria", "16GB", "tela", "14"));

        assertThat(produtos.findBySku("CAM-1").orElseThrow().atributos()).containsEntry("tamanho", "M");
        assertThat(produtos.findBySku("NOT-1").orElseThrow().atributos()).containsEntry("memoria", "16GB")
                .doesNotContainKey("tamanho");
        assertThat(catalogo.comAtributo("memoria", "16GB")).extracting(Produto::sku).containsExactly("NOT-1");
    }

    @Test
    @DisplayName("índices: sku único e (categoria, preco) composto, criados ao subir")
    void indices() {
        List<String> nomes = mongo.indexOps(Produto.class).getIndexInfo().stream()
                .map(i -> i.getIndexFields().stream().map(f -> f.getKey()).toList().toString())
                .toList();

        assertThat(nomes).contains("[sku]", "[categoria, preco]");
        assertThat(mongo.indexOps(Produto.class).getIndexInfo())
                .anyMatch(i -> i.isUnique() && i.getIndexFields().getFirst().getKey().equals("sku"));
    }

    @Test
    @DisplayName("sku repetido é recusado pelo índice único")
    void skuUnico() {
        salvar("DUP-1", "roupas", "10.00", 1, Map.of());

        assertThatThrownBy(() -> salvar("DUP-1", "roupas", "20.00", 1, Map.of()))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("faixa de preço fechada e numérica: 9.90 a 100.00 inclui as pontas e 10.00, ordenado e paginado")
    void faixaDePreco() {
        salvar("F-1", "livros", "9.90", 1, Map.of());
        salvar("F-2", "livros", "10.00", 1, Map.of());
        salvar("F-3", "livros", "99.90", 1, Map.of());
        salvar("F-4", "livros", "100.01", 1, Map.of());
        salvar("F-5", "livros", "5.00", 1, Map.of());
        salvar("F-6", "roupas", "50.00", 1, Map.of());

        Page<Produto> pagina = catalogo.porFaixaDePreco("livros", new BigDecimal("9.90"), new BigDecimal("100.00"),
                PageRequest.of(0, 2, Sort.by("preco")));

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getContent()).extracting(Produto::sku).containsExactly("F-1", "F-2");
        Document bruto = mongo.getCollection("produtos").find(new Document("sku", "F-1")).first();
        assertThat(bruto.get("preco")).as("preço gravado como número (Decimal128), não como texto")
                .isInstanceOf(Decimal128.class);
    }

    @Test
    @DisplayName("busca por atributo: atributos.cor = azul")
    void porAtributo() {
        salvar("A-1", "roupas", "10.00", 1, Map.of("cor", "azul"));
        salvar("A-2", "roupas", "10.00", 1, Map.of("cor", "verde"));
        salvar("A-3", "calcados", "10.00", 1, Map.of("cor", "azul", "numero", "40"));

        assertThat(catalogo.comAtributo("cor", "azul")).extracting(Produto::sku).containsExactly("A-1", "A-3");
    }

    @Test
    @DisplayName("nome de atributo vindo do usuário é validado: nada de operadores nem caminhos")
    void atributoInvalido() {
        for (String ruim : List.of("$where", "cor.x", "cor x", "", "Cor")) {
            assertThatThrownBy(() -> catalogo.comAtributo(ruim, "azul")).as(ruim)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("reservar é atômico: 40 pedidos simultâneos de 1 unidade num estoque de 10 vendem exatamente 10")
    void reservaAtomica() throws Exception {
        salvar("EST-1", "roupas", "10.00", 10, Map.of());
        CountDownLatch largada = new CountDownLatch(1);
        List<Callable<Boolean>> pedidos = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            pedidos.add(() -> {
                largada.await();
                return catalogo.reservar("EST-1", 1);
            });
        }

        int vendidos = 0;
        try (ExecutorService executor = Executors.newFixedThreadPool(40)) {
            List<Future<Boolean>> resultados = pedidos.stream().map(executor::submit).toList();
            largada.countDown();
            for (Future<Boolean> r : resultados) {
                if (r.get(10, TimeUnit.SECONDS)) {
                    vendidos++;
                }
            }
        }

        assertThat(vendidos).isEqualTo(10);
        assertThat(produtos.findBySku("EST-1").orElseThrow().estoque()).isZero();
    }

    @Test
    @DisplayName("reservar mais do que há: false, e o estoque não muda")
    void reservaSemEstoque() {
        salvar("EST-2", "roupas", "10.00", 3, Map.of());

        assertThat(catalogo.reservar("EST-2", 5)).isFalse();
        assertThat(produtos.findBySku("EST-2").orElseThrow().estoque()).isEqualTo(3);
        assertThat(catalogo.reservar("EST-2", 3)).isTrue();
    }

    @Test
    @DisplayName("avaliações: o documento guarda só as 10 mais recentes, mas a média considera todas")
    void avaliacoesLimitadas() {
        salvar("AV-1", "livros", "30.00", 1, Map.of());
        for (int i = 1; i <= 15; i++) {
            catalogo.avaliar("AV-1", new Avaliacao("leitor" + i, i <= 5 ? 1 : 5, "comentário " + i));
        }

        Produto produto = produtos.findBySku("AV-1").orElseThrow();
        assertThat(produto.avaliacoes()).hasSize(10);
        assertThat(produto.avaliacoes().getFirst().autor()).isEqualTo("leitor6");
        assertThat(produto.avaliacoes().getLast().autor()).isEqualTo("leitor15");
        assertThat(produto.totalAvaliacoes()).isEqualTo(15);
        assertThat(produto.notaMedia()).isCloseTo(55.0 / 15, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    @DisplayName("avaliar um sku inexistente é erro")
    void avaliarInexistente() {
        assertThatThrownBy(() -> catalogo.avaliar("NAO-EXISTE", new Avaliacao("x", 3, "")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("resumo por categoria: quantidade e preço médio calculados no servidor")
    void resumo() {
        salvar("R-1", "livros", "10.00", 1, Map.of());
        salvar("R-2", "livros", "20.00", 1, Map.of());
        salvar("R-3", "livros", "33.33", 1, Map.of());
        salvar("R-4", "roupas", "100.00", 1, Map.of());

        assertThat(catalogo.resumoPorCategoria()).containsExactly(
                new ResumoDaCategoria("livros", 3, new BigDecimal("21.11")),
                new ResumoDaCategoria("roupas", 1, new BigDecimal("100.00")));
    }
}
