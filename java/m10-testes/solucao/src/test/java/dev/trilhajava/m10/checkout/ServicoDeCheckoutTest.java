package dev.trilhajava.m10.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Suíte de referência do exercício 10.02. Given/when/then com BDDMockito, um comportamento por teste,
 * e verificação de interação SÓ onde a interação é o comportamento: cobrar, liberar estoque, salvar, notificar.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Checkout")
class ServicoDeCheckoutTest {

    static final Instant AGORA = Instant.parse("2026-09-28T13:00:00Z");

    // Quantidade maior que 1 e dois itens: com um item de quantidade 1, metade dos defeitos passaria despercebida.
    static final Item CAFE = new Item("CAFE-1KG", 2, new BigDecimal("89.90"));
    static final Item MOEDOR = new Item("MOEDOR", 1, new BigDecimal("149.00"));
    static final Pedido PEDIDO = new Pedido("cliente-7", List.of(CAFE, MOEDOR));   // total: 2 × 89,90 + 149,00 = 328,80

    @Mock
    Estoque estoque;
    @Mock
    GatewayDePagamento gateway;
    @Mock
    RepositorioDePedidos repositorio;
    @Mock
    Notificador notificador;

    ServicoDeCheckout checkout;

    @BeforeEach
    void criar() {
        // O relógio é um fake (Clock.fixed), não um mock: o tempo vira um dado do teste.
        checkout = new ServicoDeCheckout(estoque, gateway, repositorio, notificador, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Nested
    @DisplayName("pagamento aprovado")
    class Aprovado {

        @BeforeEach
        void dado() {
            given(estoque.reservar(anyString(), anyInt())).willReturn(true);
            given(gateway.cobrar(any(), any())).willReturn(ResultadoPagamento.aprovado("AUT-123"));
        }

        @Test
        @DisplayName("cobra do cliente o total exato: quantidade × preço de cada item")
        void cobraOTotal() {
            checkout.finalizar(PEDIDO);

            var valor = ArgumentCaptor.forClass(BigDecimal.class);
            then(gateway).should().cobrar(eq("cliente-7"), valor.capture());
            assertThat(valor.getValue()).isEqualByComparingTo("328.80");
        }

        @Test
        @DisplayName("salva e devolve o pedido confirmado, com total, autorização e a hora do relógio")
        void salva() {
            PedidoConfirmado devolvido = checkout.finalizar(PEDIDO);

            var salvo = ArgumentCaptor.forClass(PedidoConfirmado.class);
            then(repositorio).should().salvar(salvo.capture());
            assertThat(salvo.getValue()).isEqualTo(devolvido);
            assertThat(devolvido.numero()).startsWith("PED-");
            assertThat(devolvido.clienteId()).isEqualTo("cliente-7");
            assertThat(devolvido.total()).isEqualByComparingTo("328.80");
            assertThat(devolvido.codigoAutorizacao()).isEqualTo("AUT-123");
            assertThat(devolvido.confirmadoEm()).isEqualTo(AGORA);
        }

        @Test
        @DisplayName("notifica a confirmação, com o número do pedido, só DEPOIS de salvar")
        void notificaDepoisDeSalvar() {
            PedidoConfirmado devolvido = checkout.finalizar(PEDIDO);

            InOrder ordem = inOrder(repositorio, notificador);
            ordem.verify(repositorio).salvar(devolvido);
            ordem.verify(notificador).pedidoConfirmado("cliente-7", devolvido.numero());
        }

        @Test
        @DisplayName("não libera o estoque de um pedido confirmado")
        void naoLibera() {
            checkout.finalizar(PEDIDO);

            then(estoque).should(never()).liberar(anyString(), anyInt());
        }
    }

    @Test
    @DisplayName("falta de estoque: libera os itens já reservados, com as mesmas quantidades, e não cobra")
    void semEstoque() {
        // Três itens, e o que falta é o ÚLTIMO: só assim dá para ver se TODOS os anteriores foram liberados.
        Item bule = new Item("BULE", 3, new BigDecimal("90.00"));
        given(estoque.reservar("CAFE-1KG", 2)).willReturn(true);
        given(estoque.reservar("MOEDOR", 1)).willReturn(true);
        given(estoque.reservar("BULE", 3)).willReturn(false);

        assertThatThrownBy(() -> checkout.finalizar(new Pedido("cliente-7", List.of(CAFE, MOEDOR, bule))))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("BULE");

        then(estoque).should().liberar("CAFE-1KG", 2);
        then(estoque).should().liberar("MOEDOR", 1);
        then(estoque).should(never()).liberar(eq("BULE"), anyInt());
        verifyNoInteractions(gateway, repositorio, notificador);
    }

    @Test
    @DisplayName("pagamento recusado: libera tudo, avisa o cliente com o motivo e não salva nem confirma")
    void recusado() {
        given(estoque.reservar(anyString(), anyInt())).willReturn(true);
        given(gateway.cobrar(any(), any())).willReturn(ResultadoPagamento.recusado("saldo insuficiente no cartão"));

        assertThatThrownBy(() -> checkout.finalizar(PEDIDO))
                .isInstanceOf(PagamentoRecusadoException.class)
                .hasMessageContaining("saldo insuficiente no cartão");

        then(estoque).should().liberar("CAFE-1KG", 2);
        then(estoque).should().liberar("MOEDOR", 1);
        then(notificador).should().pagamentoRecusado("cliente-7", "saldo insuficiente no cartão");
        then(notificador).should(never()).pedidoConfirmado(any(), any());
        then(repositorio).should(never()).salvar(any());
    }

    @Test
    @DisplayName("gateway fora do ar: libera as reservas e deixa a exceção sair, sem salvar nem notificar")
    void gatewayIndisponivel() {
        given(estoque.reservar(anyString(), anyInt())).willReturn(true);
        given(gateway.cobrar(any(), any())).willThrow(new GatewayIndisponivelException("timeout"));

        assertThatThrownBy(() -> checkout.finalizar(PEDIDO)).isInstanceOf(GatewayIndisponivelException.class);

        then(estoque).should().liberar("CAFE-1KG", 2);
        then(estoque).should().liberar("MOEDOR", 1);
        verifyNoInteractions(repositorio, notificador);
    }

    @Nested
    @DisplayName("pedido inválido: IllegalArgumentException sem tocar em nenhuma dependência")
    class Invalido {

        @Test
        @DisplayName("sem itens")
        void semItens() {
            assertThatIllegalArgumentException().isThrownBy(() -> checkout.finalizar(new Pedido("cliente-7", List.of())));
            verifyNoInteractions(estoque, gateway, repositorio, notificador);
        }

        @Test
        @DisplayName("item com quantidade zero ou negativa")
        void quantidadeInvalida() {
            Item zero = new Item("CAFE-1KG", 0, new BigDecimal("89.90"));
            Item negativo = new Item("MOEDOR", -1, new BigDecimal("149.00"));

            assertThatIllegalArgumentException().isThrownBy(() -> checkout.finalizar(new Pedido("cliente-7", List.of(zero))));
            assertThatIllegalArgumentException().isThrownBy(
                    () -> checkout.finalizar(new Pedido("cliente-7", List.of(CAFE, negativo))));
            verifyNoInteractions(estoque, gateway, repositorio, notificador);
        }
    }
}
