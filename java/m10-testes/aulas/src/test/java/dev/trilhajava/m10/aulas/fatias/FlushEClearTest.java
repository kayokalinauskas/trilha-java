package dev.trilhajava.m10.aulas.fatias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

/** O que está no cache de primeiro nível não é o que está no banco: flush() e clear() mostram a diferença. */
@DataJpaTest
class FlushEClearTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProdutoRepository produtos;

    Categoria mercearia;

    @BeforeEach
    void umaCategoria() {
        mercearia = em.persist(new Categoria("Mercearia"));
    }

    @Test
    void semFlushEClearVoceTestaOObjetoNaMemoriaNaoOBanco() {
        Produto cha = em.persist(new Produto("CHA-100G", "Chá verde 100 g", new BigDecimal("19.9"), 7, mercearia));

        // Ainda no cache de primeiro nível: findById devolve a MESMA instância, sem ir ao banco.
        assertThat(produtos.findById(cha.getId())).containsSame(cha);

        em.flush();   // INSERT
        em.clear();   // esquece as instâncias gerenciadas

        Produto doBanco = produtos.findById(cha.getId()).orElseThrow();   // SELECT de verdade
        assertThat(doBanco).isNotSameAs(cha);
        assertThat(doBanco.getPreco()).isEqualTo(new BigDecimal("19.90"));   // a coluna numeric(10,2) devolve 2 casas
    }

    @Test
    void skuDuplicadoEsbarraNaRestricaoUniqueNoFlush() {
        produtos.save(new Produto("CAFE-1KG", "Café torrado 1 kg", new BigDecimal("49.90"), 12, mercearia));
        Produto duplicado = new Produto("CAFE-1KG", "Outro café", new BigDecimal("10.00"), 1, mercearia);

        assertThatThrownBy(() -> produtos.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
