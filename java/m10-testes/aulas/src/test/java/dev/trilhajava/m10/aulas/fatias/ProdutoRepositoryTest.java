package dev.trilhajava.m10.aulas.fatias;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
class ProdutoRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProdutoRepository produtos;

    @BeforeEach
    void catalogo() {
        Categoria mercearia = em.persist(new Categoria("Mercearia"));
        Categoria utilidades = em.persist(new Categoria("Utilidades"));
        em.persist(new Produto("CAFE-1KG", "Café torrado 1 kg", new BigDecimal("49.90"), 12, mercearia));
        em.persist(new Produto("ACUCAR-1KG", "Açúcar cristal 1 kg", new BigDecimal("5.50"), 3, mercearia));
        em.persist(new Produto("FILTRO-102", "Filtro de papel 102", new BigDecimal("12.50"), 5, utilidades));
        Produto garrafa = em.persist(
                new Produto("GARRAFA-1L", "Garrafa térmica 1 L", new BigDecimal("89.00"), 1, utilidades));
        garrafa.desativar();
        em.flush();   // INSERTs e UPDATE vão para o banco agora
        em.clear();   // e o cache de primeiro nível é esvaziado: as consultas abaixo leem do banco
    }

    @Test
    void buscaPorSku() {
        assertThat(produtos.findBySku("CAFE-1KG"))
                .hasValueSatisfying(p -> {
                    assertThat(p.getNome()).isEqualTo("Café torrado 1 kg");
                    assertThat(p.getPreco()).isEqualByComparingTo("49.90");
                    assertThat(p.getCategoria().getNome()).isEqualTo("Mercearia");
                });
        assertThat(produtos.findBySku("NAO-EXISTE")).isEmpty();
    }

    @Test
    void estoqueBaixoIgnoraInativosEOLimiteExato() {
        // limite 5: entra quem tem MENOS de 5; o filtro (5) fica de fora, a garrafa (1) está inativa
        assertThat(produtos.findByAtivoTrueAndQuantidadeEmEstoqueLessThanOrderBySku(5))
                .extracting(Produto::getSku)
                .containsExactly("ACUCAR-1KG");
    }

    @Test
    void valorEmEstoquePorCategoria() {
        // Mercearia: 12 × 49,90 + 3 × 5,50 = 615,30. Utilidades: 5 × 12,50 = 62,50 (a garrafa inativa não conta)
        var bigDecimalPeloValor = RecursiveComparisonConfiguration.builder()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .build();

        assertThat(produtos.valorEmEstoquePorCategoria())
                .usingRecursiveFieldByFieldElementComparator(bigDecimalPeloValor)
                .containsExactly(
                        new ValorPorCategoria("Mercearia", new BigDecimal("615.30")),
                        new ValorPorCategoria("Utilidades", new BigDecimal("62.50")));
    }

    @Test
    void cadaTesteComecaSoComOsDadosDoBeforeEach() {
        // Se o INSERT de outro teste tivesse sido confirmado, a contagem seria outra.
        assertThat(produtos.count()).isEqualTo(4);
    }
}
