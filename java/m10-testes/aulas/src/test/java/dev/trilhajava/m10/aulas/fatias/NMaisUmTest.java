package dev.trilhajava.m10.aulas.fatias;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/** Conta os comandos SQL que o Hibernate prepara: é assim que um N+1 vira um teste vermelho. */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class NMaisUmTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProdutoRepository produtos;

    @Autowired
    EntityManagerFactory emf;

    Statistics estatisticas;

    @BeforeEach
    void tresProdutosDeTresCategorias() {
        for (String nome : List.of("Mercearia", "Utilidades", "Bebidas")) {
            Categoria categoria = em.persist(new Categoria(nome));
            em.persist(new Produto("SKU-" + nome.toUpperCase(), "Produto de " + nome, BigDecimal.TEN, 1, categoria));
        }
        em.flush();
        em.clear();
        estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();
    }

    @Test
    void consultaDerivadaMaisCategoriaLazyDaNMaisUm() {
        List<String> categorias = produtos.findByAtivoTrueOrderByNome().stream()
                .map(p -> p.getCategoria().getNome())   // cada acesso dispara um SELECT na categoria
                .toList();

        assertThat(categorias).containsExactly("Bebidas", "Mercearia", "Utilidades");
        assertThat(estatisticas.getPrepareStatementCount()).as("comandos SQL").isEqualTo(4);   // 1 + N (N = 3)
    }

    @Test
    void joinFetchResolveComUmaConsulta() {
        List<String> categorias = produtos.listarAtivosComCategoria().stream()
                .map(p -> p.getCategoria().getNome())
                .toList();

        assertThat(categorias).containsExactly("Bebidas", "Mercearia", "Utilidades");
        assertThat(estatisticas.getPrepareStatementCount()).as("comandos SQL").isEqualTo(1);
    }
}
