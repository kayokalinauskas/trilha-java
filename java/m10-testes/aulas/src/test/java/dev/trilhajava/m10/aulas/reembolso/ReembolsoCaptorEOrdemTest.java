package dev.trilhajava.m10.aulas.reembolso;

import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.AGORA;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.RELOGIO;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.pedidoDentroDoPrazo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReembolsoCaptorEOrdemTest {

    @Mock RepositorioDePedidos pedidos;
    @Mock GatewayDePagamento gateway;
    @Mock Estoque estoque;
    @Mock RepositorioDeReembolsos reembolsos;
    @Mock Notificador notificador;

    @Captor ArgumentCaptor<Reembolso> reembolsoSalvo;

    ServicoDeReembolso servico;

    @BeforeEach
    void criarServico() {
        servico = new ServicoDeReembolso(pedidos, gateway, estoque, reembolsos, notificador, RELOGIO);
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoDentroDoPrazo()));
    }

    @Test
    @DisplayName("salva o reembolso com o valor, o código do estorno e o instante do relógio")
    void salvaOReembolso() {
        given(gateway.estornar(any(), any())).willReturn("EST-1");

        servico.reembolsar("PED-100");

        then(reembolsos).should().salvar(reembolsoSalvo.capture());
        Reembolso salvo = reembolsoSalvo.getValue();
        assertThat(salvo.numeroPedido()).isEqualTo("PED-100");
        assertThat(salvo.valor()).isEqualByComparingTo("112.30");
        assertThat(salvo.codigoEstorno()).isEqualTo("EST-1");
        assertThat(salvo.realizadoEm()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("estorna, depois salva e só então avisa o cliente")
    void ordemDosEfeitos() {
        servico.reembolsar("PED-100");

        InOrder ordem = inOrder(gateway, reembolsos, notificador);
        then(gateway).should(ordem).estornar(any(), any());
        then(reembolsos).should(ordem).salvar(any());
        then(notificador).should(ordem).reembolsoConcluido(eq("cliente-42"), eq("PED-100"), any());
    }
}
