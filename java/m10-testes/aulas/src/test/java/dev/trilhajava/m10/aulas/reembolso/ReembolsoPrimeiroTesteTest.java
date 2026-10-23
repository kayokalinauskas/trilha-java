package dev.trilhajava.m10.aulas.reembolso;

import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.AGORA;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.RELOGIO;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.pedidoDentroDoPrazo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReembolsoPrimeiroTesteTest {

    @Mock RepositorioDePedidos pedidos;
    @Mock GatewayDePagamento gateway;
    @Mock Estoque estoque;
    @Mock RepositorioDeReembolsos reembolsos;
    @Mock Notificador notificador;

    ServicoDeReembolso servico;

    @BeforeEach
    void criarServico() {
        // O Clock não é um mock: é um relógio de verdade, só que parado.
        servico = new ServicoDeReembolso(pedidos, gateway, estoque, reembolsos, notificador, RELOGIO);
    }

    @Test
    @DisplayName("reembolsa o total do pedido e devolve o código do estorno")
    void reembolsaOTotal() {
        // Arrange: o que as dependências respondem
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoDentroDoPrazo()));
        given(gateway.estornar("AUT-777", new BigDecimal("112.30"))).willReturn("EST-1");

        // Act
        Reembolso reembolso = servico.reembolsar("PED-100");

        // Assert: o que o serviço devolveu
        assertThat(reembolso.numeroPedido()).isEqualTo("PED-100");
        assertThat(reembolso.valor()).isEqualByComparingTo("112.30");
        assertThat(reembolso.codigoEstorno()).isEqualTo("EST-1");
        assertThat(reembolso.realizadoEm()).isEqualTo(AGORA);
    }
}
