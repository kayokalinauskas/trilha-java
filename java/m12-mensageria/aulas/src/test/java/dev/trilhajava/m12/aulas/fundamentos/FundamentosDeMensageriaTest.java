package dev.trilhajava.m12.aulas.fundamentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class FundamentosDeMensageriaTest {

    private final PedidoPago ped1 = Exemplos.pedidoPago("PED-2001", "cli-7", "259.90");
    private final PedidoPago ped2 = Exemplos.pedidoPago("PED-2002", "cli-7", "100.00");

    @Test
    void naCadeiaSincronaUmVizinhoForaDoArDerrubaOCheckout() {
        var nota = new ServicoSimulado("nota-fiscal", Duration.ofMillis(180));
        var email = new ServicoSimulado("e-mail", Duration.ofMillis(350));
        email.derrubar();

        assertThatThrownBy(() -> new CheckoutSincrono(List.of(nota, email)).pagar(ped1))
                .isInstanceOf(ServicoIndisponivelException.class);
        assertThat(nota.atendidos()).containsExactly("PED-2001"); // efeito parcial: a nota saiu, o pedido não
    }

    @Test
    void comEventosOConsumidorForaDoArSoAtrasa() {
        var log = new LogDeEventos<PedidoPago>();
        var email = new ServicoSimulado("e-mail", Duration.ofMillis(350));
        var consumidor = new ConsumidorDoLog(log, "email", email, Garantia.PELO_MENOS_UMA_VEZ, true);
        email.derrubar();
        log.publicar(ped1);

        assertThatThrownBy(consumidor::processarPendentes).isInstanceOf(ServicoIndisponivelException.class);
        assertThat(log.atraso("email")).isEqualTo(1);

        email.religar();
        assertThat(consumidor.processarPendentes()).isEqualTo(1);
        assertThat(email.atendidos()).containsExactly("PED-2001");
        assertThat(log.atraso("email")).isZero();
    }

    @Test
    void aOrdemEntreConfirmarEProcessarDefineAGarantia() {
        assertThat(saldoDepoisDeUmaQueda(Garantia.NO_MAXIMO_UMA_VEZ, false)).isEqualTo(100);  // perdeu 259
        assertThat(saldoDepoisDeUmaQueda(Garantia.PELO_MENOS_UMA_VEZ, false)).isEqualTo(618); // 259 em dobro
        assertThat(saldoDepoisDeUmaQueda(Garantia.PELO_MENOS_UMA_VEZ, true)).isEqualTo(359);  // correto
    }

    @Test
    void filaApagaNoAckELogGuardaParaOutrosGrupos() {
        var fila = new FilaDeTrabalho<String>();
        fila.enviar("separar PED-1");
        var entrega = fila.receber().orElseThrow();
        fila.consumidorCaiu();
        assertThat(fila.receber().orElseThrow().reentregue()).isTrue();

        var log = new LogDeEventos<PedidoPago>();
        log.publicar(ped1);
        log.publicar(ped2);
        log.confirmar("faturamento", 2);
        assertThat(log.ler("faturamento", 10)).isEmpty();
        assertThat(log.ler("dados", 10)).extracting(LogDeEventos.Registro::offset).containsExactly(0L, 1L);
        assertThat(entrega.reentregue()).isFalse();
    }

    private int saldoDepoisDeUmaQueda(Garantia garantia, boolean idempotente) {
        var log = new LogDeEventos<PedidoPago>();
        log.publicar(ped1);
        log.publicar(ped2);
        var fidelidade = new ProgramaDeFidelidade();
        var consumidor = new ConsumidorDoLog(log, "fidelidade", fidelidade, garantia, idempotente);
        consumidor.cairNoMeioDoProximo();
        assertThatThrownBy(consumidor::processarPendentes).isInstanceOf(QuedaSimuladaException.class);
        consumidor.processarPendentes();
        return fidelidade.saldo("cli-7");
    }
}
