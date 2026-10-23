package dev.trilhajava.m12.aulas.esquemas;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O portão de compatibilidade no CI: TODO exemplo de evento já publicado em produção
 * (um arquivo por versão e por variação relevante) precisa continuar legível pelo consumidor atual.
 * Exemplos nunca são apagados; quando o produtor lança uma versão nova, ganha um arquivo novo.
 */
class CompatibilidadeDeEsquemasTest {

    private final LeitorDePedidoConfirmado leitor = Exemplos.leitor();

    @TestFactory
    Stream<DynamicTest> todoEventoJaPublicadoContinuaLegivel() {
        return Exemplos.todos().stream().map(arquivo -> DynamicTest.dynamicTest(
                arquivo.getFileName().toString(),
                () -> {
                    PedidoConfirmado pedido = leitor.ler(Exemplos.ler(arquivo.getFileName().toString()));
                    assertThat(pedido.pedidoId()).startsWith("PED-");
                    assertThat(pedido.valorTotal().quantia()).isPositive();
                }));
    }
}
