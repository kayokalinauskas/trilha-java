package dev.trilhajava.m13.aulas.solid;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Liskov na prática: o mesmo teste roda contra TODAS as implementações da interface. */
class ContratoDasRegrasDeDescontoTest {

    static Stream<RegraDeDesconto> regras() {
        return Stream.of(new DescontoVip(), new DescontoPrimeiraCompra(), new DescontoAtacado(),
                new DescontoAniversario());
    }

    @ParameterizedTest
    @MethodSource("regras")
    void todaRegraCumpreOContrato(RegraDeDesconto regra) {
        String nome = regra.getClass().getSimpleName();
        var aleatorio = new Random(7);
        for (int i = 0; i < 500; i++) {
            Carrinho carrinho = GeradorDeCarrinhos.aleatorio(aleatorio);
            var resultado = regra.avaliar(carrinho);
            assertThat(resultado).as("%s devolveu null", nome).isNotNull();
            resultado.ifPresent(desconto -> {
                assertThat(desconto.valor()).as("%s: valor fora de [0, subtotal]", nome)
                        .isBetween(BigDecimal.ZERO, carrinho.subtotal());
                assertThat(desconto.valor().scale()).as("%s: escala de %s", nome, desconto.valor())
                        .isEqualTo(2);
                assertThat(desconto.motivo()).as("%s: motivo vazio", nome).isNotBlank();
            });
        }
    }
}
