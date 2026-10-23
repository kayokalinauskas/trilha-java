package dev.trilhajava.m10.aulas.reembolso;

import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.RELOGIO;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.pedidoDentroDoPrazo;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.pedidoEntregueEm;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReembolsoFalhasTest {

    @Mock RepositorioDePedidos pedidos;
    @Mock GatewayDePagamento gateway;
    @Mock Estoque estoque;
    @Mock RepositorioDeReembolsos reembolsos;
    @Mock Notificador notificador;

    ServicoDeReembolso servico;

    @BeforeEach
    void criarServico() {
        servico = new ServicoDeReembolso(pedidos, gateway, estoque, reembolsos, notificador, RELOGIO);
    }

    @Test
    @DisplayName("gateway fora do ar: a exceção sobe e nada volta ao estoque, nada é salvo, ninguém é avisado")
    void gatewayForaDoAr() {
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoDentroDoPrazo()));
        willThrow(new GatewayIndisponivelException("timeout depois de 5s"))
                .given(gateway).estornar(any(), any());

        assertThatThrownBy(() -> servico.reembolsar("PED-100"))
                .isInstanceOf(GatewayIndisponivelException.class)
                .hasMessageContaining("timeout");

        then(estoque).shouldHaveNoInteractions();
        then(reembolsos).shouldHaveNoInteractions();
        then(notificador).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("entregue há 30 dias: recusa o reembolso sem estornar nada")
    void foraDoPrazo() {
        // RELOGIO marca 10/09/2026; a entrega foi em 11/08/2026.
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoEntregueEm(LocalDate.of(2026, 8, 11))));

        assertThatThrownBy(() -> servico.reembolsar("PED-100"))
                .isInstanceOf(ForaDoPrazoException.class)
                .hasMessageContaining("2026-08-18");

        then(gateway).shouldHaveNoInteractions();
        then(estoque).shouldHaveNoInteractions();
        then(reembolsos).shouldHaveNoInteractions();
        then(notificador).shouldHaveNoInteractions();
    }
}
