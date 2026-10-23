package dev.trilhajava.m10.aulas.reembolso;

import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.SAO_PAULO;
import static dev.trilhajava.m10.aulas.reembolso.DadosDeTeste.pedidoEntregueEm;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Solução do checkpoint: a borda do prazo de 7 dias. */
@ExtendWith(MockitoExtension.class)
class ReembolsoPrazoLimiteTest {

    static final LocalDate ENTREGA = LocalDate.of(2026, 9, 1);

    @Mock RepositorioDePedidos pedidos;
    @Mock GatewayDePagamento gateway;
    @Mock Estoque estoque;
    @Mock RepositorioDeReembolsos reembolsos;
    @Mock Notificador notificador;

    /** Um serviço cujo relógio marca 23h59 do dia informado, no fuso de São Paulo. */
    ServicoDeReembolso servicoNoDia(LocalDate dia) {
        Clock relogio = Clock.fixed(dia.atTime(23, 59).atZone(SAO_PAULO).toInstant(), SAO_PAULO);
        return new ServicoDeReembolso(pedidos, gateway, estoque, reembolsos, notificador, relogio);
    }

    @Test
    @DisplayName("no 7º dia depois da entrega, ainda reembolsa")
    void seteDiasDepois() {
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoEntregueEm(ENTREGA)));

        servicoNoDia(ENTREGA.plusDays(7)).reembolsar("PED-100");

        then(gateway).should().estornar("AUT-777", new BigDecimal("112.30"));
    }

    @Test
    @DisplayName("no 8º dia depois da entrega, recusa e não estorna")
    void oitoDiasDepois() {
        given(pedidos.buscar("PED-100")).willReturn(Optional.of(pedidoEntregueEm(ENTREGA)));

        assertThatThrownBy(() -> servicoNoDia(ENTREGA.plusDays(8)).reembolsar("PED-100"))
                .isInstanceOf(ForaDoPrazoException.class);

        then(gateway).should(never()).estornar(any(), any());
    }
}
