package dev.trilhajava.m10.aulas.reembolso;

import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.RELOGIO;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.pedidoDentroDoPrazo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReembolsoInteracoesTest {

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
    @DisplayName("estorna o total na autorização da cobrança original")
    void estornaOTotal() {
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoDentroDoPrazo()));

        servico.reembolsar("PED-100");

        then(gateway).should().estornar("AUT-777", new BigDecimal("112.30"));
    }

    @Test
    @DisplayName("devolve cada item ao estoque com a quantidade comprada")
    void devolveOsItens() {
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoDentroDoPrazo()));

        servico.reembolsar("PED-100");

        then(estoque).should().devolver("CAFE-1KG", 2);
        then(estoque).should().devolver("FILTRO-102", 1);
        then(estoque).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("avisa o cliente com o número do pedido e o valor")
    void avisaOCliente() {
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoDentroDoPrazo()));

        servico.reembolsar("PED-100");

        then(notificador).should().reembolsoConcluido("cliente-42", "PED-100", new BigDecimal("112.30"));
    }

    @Test
    @DisplayName("pedido inexistente: não estorna, não mexe no estoque, não salva e não avisa")
    void pedidoInexistente() {
        given(pedidos.buscar("PED-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> servico.reembolsar("PED-999"))
                .isInstanceOf(PedidoNaoEncontradoException.class);

        then(gateway).should(never()).estornar(any(), any());
        then(estoque).shouldHaveNoInteractions();
        then(reembolsos).shouldHaveNoInteractions();
        then(notificador).shouldHaveNoInteractions();
    }
}
