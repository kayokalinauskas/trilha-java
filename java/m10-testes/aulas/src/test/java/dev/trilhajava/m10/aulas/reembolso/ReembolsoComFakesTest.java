package dev.trilhajava.m10.aulas.reembolso;

import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.AGORA;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.RELOGIO;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.pedidoDentroDoPrazo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * As mesmas regras, verificadas pelo ESTADO: fakes para o que é nosso (repositórios e estoque),
 * mocks só para as fronteiras com o mundo externo (gateway e notificação).
 */
@ExtendWith(MockitoExtension.class)
class ReembolsoComFakesTest {

    RepositorioDePedidosEmMemoria pedidos = new RepositorioDePedidosEmMemoria().com(pedidoDentroDoPrazo());
    EstoqueEmMemoria estoque = new EstoqueEmMemoria().comSaldo("CAFE-1KG", 10).comSaldo("FILTRO-102", 5);
    RepositorioDeReembolsosEmMemoria reembolsos = new RepositorioDeReembolsosEmMemoria();

    @Mock GatewayDePagamento gateway;
    @Mock Notificador notificador;

    ServicoDeReembolso servico;

    @BeforeEach
    void criarServico() {
        servico = new ServicoDeReembolso(pedidos, gateway, estoque, reembolsos, notificador, RELOGIO);
    }

    @Test
    @DisplayName("depois do reembolso, o estoque aumentou e o reembolso está registrado")
    void estadoDepoisDoReembolso() {
        given(gateway.estornar(any(), any())).willReturn("EST-1");

        servico.reembolsar("PED-100");

        assertThat(estoque.saldo("CAFE-1KG")).isEqualTo(12);
        assertThat(estoque.saldo("FILTRO-102")).isEqualTo(6);
        assertThat(reembolsos.todos())
                .containsExactly(new Reembolso("PED-100", new BigDecimal("112.30"), "EST-1", AGORA));
    }

    @Test
    @DisplayName("gateway fora do ar: o estoque e os reembolsos continuam como estavam")
    void gatewayForaDoAr() {
        willThrow(new GatewayIndisponivelException("timeout")).given(gateway).estornar(any(), any());

        assertThatThrownBy(() -> servico.reembolsar("PED-100"))
                .isInstanceOf(GatewayIndisponivelException.class);

        assertThat(estoque.saldo("CAFE-1KG")).isEqualTo(10);
        assertThat(estoque.saldo("FILTRO-102")).isEqualTo(5);
        assertThat(reembolsos.todos()).isEmpty();
    }
}
