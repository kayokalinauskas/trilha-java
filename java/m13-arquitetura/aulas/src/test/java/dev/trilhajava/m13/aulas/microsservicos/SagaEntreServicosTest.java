package dev.trilhajava.m13.aulas.microsservicos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import dev.trilhajava.m13.aulas.microsservicos.estoque.Estoque;
import dev.trilhajava.m13.aulas.microsservicos.estoque.EstoqueAplicacao;
import dev.trilhajava.m13.aulas.microsservicos.pedidos.ClientesHttp;
import dev.trilhajava.m13.aulas.microsservicos.pedidos.EstoqueCliente;
import dev.trilhajava.m13.aulas.microsservicos.pedidos.Pagamentos;
import dev.trilhajava.m13.aulas.microsservicos.pedidos.SagaDeCriacaoDePedido;
import dev.trilhajava.m13.aulas.microsservicos.pedidos.SagaDeCriacaoDePedido.NovoPedido;
import dev.trilhajava.m13.aulas.microsservicos.pedidos.SagaDeCriacaoDePedido.Resultado;
import dev.trilhajava.m13.aulas.microsservicos.pedidos.SagaDeCriacaoDePedido.Status;

/** O serviço de estoque sobe de verdade numa porta aleatória; o de pedidos fala com ele por HTTP. */
@SpringBootTest(classes = EstoqueAplicacao.class, webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "logging.level.dev.trilhajava.m13.aulas.microsservicos=INFO")
class SagaEntreServicosTest {

    @LocalServerPort
    int porta;

    @Autowired
    Estoque estoqueDoOutroServico;   // só para preparar cenários; pedidos não enxerga isto

    EstoqueCliente estoque;

    @BeforeEach
    void montarClienteHttp() {
        estoque = ClientesHttp.estoque("http://localhost:" + porta, Duration.ofMillis(500));
    }

    @AfterEach
    void voltarAoNormal() {
        estoqueDoOutroServico.simularLentidao(Duration.ZERO);
    }

    @Test
    void caminhoFelizReservaECobra() {
        Pagamentos aprovaTudo = (pedidoId, valor) -> Pagamentos.Resposta.APROVADO;
        var saga = new SagaDeCriacaoDePedido(estoque, aprovaTudo);

        Resultado resultado = saga.executar(new NovoPedido("PED-1", "CAFE-500G", 2, new BigDecimal("99.80")));

        assertThat(resultado.status()).isEqualTo(Status.CONFIRMADO);
        assertThat(estoque.saldo("CAFE-500G").reservado()).isEqualTo(2);
    }

    @Test
    void pagamentoRecusadoLiberaAReserva() {
        Pagamentos recusaTudo = (pedidoId, valor) -> Pagamentos.Resposta.RECUSADO;
        var saga = new SagaDeCriacaoDePedido(estoque, recusaTudo);
        int antes = estoque.saldo("FILTRO-103").disponivel();

        Resultado resultado = saga.executar(new NovoPedido("PED-2", "FILTRO-103", 1, new BigDecimal("12.50")));

        assertThat(resultado.status()).isEqualTo(Status.CANCELADO);
        assertThat(resultado.passos()).containsExactly(
                "estoque reservado: 1 x FILTRO-103",
                "pagamento recusado",
                "compensação: reserva liberada");
        assertThat(estoque.saldo("FILTRO-103").disponivel()).isEqualTo(antes);
    }

    @Test
    void timeoutNaReservaTambemCompensa() {
        estoqueDoOutroServico.simularLentidao(Duration.ofSeconds(2));
        Pagamentos naoDeveriaSerChamado = (pedidoId, valor) -> {
            throw new IllegalStateException("sem reserva confirmada, não se cobra");
        };
        var saga = new SagaDeCriacaoDePedido(estoque, naoDeveriaSerChamado);
        int antes = estoque.saldo("CAFE-500G").disponivel();

        Resultado resultado = saga.executar(new NovoPedido("PED-3", "CAFE-500G", 1, new BigDecimal("49.90")));

        assertThat(resultado.status()).isEqualTo(Status.CANCELADO);
        assertThat(resultado.passos().getFirst()).startsWith("sem resposta do estoque");
        assertThat(estoqueDoOutroServico.reservaDo("PED-3")).isEmpty();   // o estoque tinha reservado!
        assertThat(estoque.saldo("CAFE-500G").disponivel()).isEqualTo(antes);
    }

    @Test
    void faltaDeSaldoRejeitaSemCobrarNemCompensar() {
        Pagamentos naoDeveriaSerChamado = (pedidoId, valor) -> {
            throw new IllegalStateException("sem reserva, não se cobra");
        };
        var saga = new SagaDeCriacaoDePedido(estoque, naoDeveriaSerChamado);

        Resultado resultado = saga.executar(new NovoPedido("PED-4", "FILTRO-103", 50, new BigDecimal("625.00")));

        assertThat(resultado.status()).isEqualTo(Status.REJEITADO);
        assertThat(resultado.passos()).containsExactly("estoque recusou a reserva (409)");
    }
}
