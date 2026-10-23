package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Como o time testava antes: a aplicação inteira, mas com um H2 em memória no lugar do PostgreSQL. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:loja;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
@DisplayName("Carrinho V1 no H2")
class CarrinhoV1NoH2Test {

    @Autowired
    ItensDoCarrinhoV1 carrinho;

    @Test
    @DisplayName("adicionar o mesmo produto duas vezes soma as quantidades")
    void somaAsQuantidades() {
        UUID carrinhoDaAna = UUID.randomUUID();

        carrinho.adicionar(carrinhoDaAna, "CAFE-1KG", 2);
        carrinho.adicionar(carrinhoDaAna, "CAFE-1KG", 1);

        assertThat(carrinho.itens(carrinhoDaAna)).containsExactly(new ItemDoCarrinho("CAFE-1KG", 3));
    }
}
