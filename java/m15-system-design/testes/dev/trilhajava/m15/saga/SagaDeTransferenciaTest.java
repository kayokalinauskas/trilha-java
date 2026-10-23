package dev.trilhajava.m15.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m15.saga.BancoParceiro.Resposta;
import dev.trilhajava.m15.saga.Transferencia.Estado;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 15.04 — saga de transferência entre bancos (orquestração e compensação)")
class SagaDeTransferenciaTest {

    /** Um parceiro roteirizado: cada chamada consome a próxima reação da fila. */
    static class ParceiroFalso implements BancoParceiro {

        /** null na fila = ficar sem responder (lança ParceiroIndisponivelException). */
        final Deque<Optional<Resposta>> envios = new ArrayDeque<>();
        final Deque<Optional<Optional<Resposta>>> consultas = new ArrayDeque<>();
        final List<String> recebidos = new ArrayList<>();
        int chamadasDeConsulta;

        ParceiroFalso responde(Resposta resposta) {
            envios.add(Optional.of(resposta));
            return this;
        }

        ParceiroFalso naoResponde() {
            envios.add(Optional.empty());
            return this;
        }

        ParceiroFalso consultaDiz(Resposta resposta) {
            consultas.add(Optional.of(Optional.ofNullable(resposta)));
            return this;
        }

        ParceiroFalso consultaNaoResponde() {
            consultas.add(Optional.empty());
            return this;
        }

        @Override
        public Resposta enviar(String idTransferencia, String contaDestino, BigDecimal valor) {
            recebidos.add(idTransferencia + " " + contaDestino + " " + valor);
            Optional<Resposta> reacao = envios.poll();
            if (reacao == null) {
                throw new AssertionError("envio inesperado ao parceiro: " + idTransferencia);
            }
            return reacao.orElseThrow(() -> new ParceiroIndisponivelException("timeout"));
        }

        @Override
        public Optional<Resposta> consultar(String idTransferencia) {
            chamadasDeConsulta++;
            Optional<Optional<Resposta>> reacao = consultas.poll();
            if (reacao == null) {
                throw new AssertionError("consulta inesperada ao parceiro: " + idTransferencia);
            }
            return reacao.orElseThrow(() -> new ParceiroIndisponivelException("timeout"));
        }
    }

    Contas contas;
    ParceiroFalso parceiro;
    OrquestradorDeTransferencias saga;

    @BeforeEach
    void preparar() {
        contas = new Contas();
        contas.abrir("ana", "100.00");
        parceiro = new ParceiroFalso();
        saga = new OrquestradorDeTransferencias(contas, parceiro);
    }

    Transferencia enviar30(String id) {
        return saga.iniciar(id, "ana", "bruno@outro-banco", new BigDecimal("30.00"));
    }

    BigDecimal saldoDaAna() {
        return contas.saldo("ana");
    }

    @Test
    @DisplayName("parceiro aceita: CONCLUIDA, saldo debitado e o parceiro recebeu id, destino e valor")
    void aceita() {
        parceiro.responde(Resposta.ACEITA);

        assertThat(enviar30("t1").estado()).isEqualTo(Estado.CONCLUIDA);
        assertThat(saldoDaAna()).isEqualByComparingTo("70.00");
        assertThat(parceiro.recebidos).containsExactly("t1 bruno@outro-banco 30.00");
    }

    @Test
    @DisplayName("sem saldo: RECUSADA, e o parceiro nem é chamado")
    void semSaldo() {
        Transferencia t = saga.iniciar("t2", "ana", "bruno@outro-banco", new BigDecimal("500.00"));

        assertThat(t.estado()).isEqualTo(Estado.RECUSADA);
        assertThat(saldoDaAna()).isEqualByComparingTo("100.00");
        assertThat(parceiro.recebidos).isEmpty();
    }

    @Test
    @DisplayName("parceiro recusa: o débito é compensado (COMPENSADA) e o saldo volta")
    void recusaCompensa() {
        parceiro.responde(Resposta.RECUSADA);

        assertThat(enviar30("t3").estado()).isEqualTo(Estado.COMPENSADA);
        assertThat(saldoDaAna()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("timeout não é falha: PENDENTE, e o débito NÃO é estornado às cegas")
    void timeoutFicaPendente() {
        parceiro.naoResponde();

        assertThat(enviar30("t4").estado()).isEqualTo(Estado.PENDENTE);
        assertThat(saldoDaAna()).isEqualByComparingTo("70.00");
    }

    @Test
    @DisplayName("reconciliação: o parceiro tinha aceitado → CONCLUIDA, sem estorno")
    void reconciliaAceita() {
        parceiro.naoResponde().consultaDiz(Resposta.ACEITA);
        enviar30("t5");

        assertThat(saga.reconciliar("t5").estado()).isEqualTo(Estado.CONCLUIDA);
        assertThat(saldoDaAna()).isEqualByComparingTo("70.00");
    }

    @Test
    @DisplayName("reconciliação: o parceiro nunca recebeu → COMPENSADA, saldo volta")
    void reconciliaNuncaRecebeu() {
        parceiro.naoResponde().consultaDiz(null);
        enviar30("t6");

        assertThat(saga.reconciliar("t6").estado()).isEqualTo(Estado.COMPENSADA);
        assertThat(saldoDaAna()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("reconciliação: o parceiro tinha recusado → COMPENSADA")
    void reconciliaRecusada() {
        parceiro.naoResponde().consultaDiz(Resposta.RECUSADA);
        enviar30("t7");

        assertThat(saga.reconciliar("t7").estado()).isEqualTo(Estado.COMPENSADA);
        assertThat(saldoDaAna()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("reconciliação sem resposta: continua PENDENTE, e dá para tentar de novo depois")
    void reconciliaDeNovo() {
        parceiro.naoResponde().consultaNaoResponde().consultaDiz(Resposta.ACEITA);
        enviar30("t8");

        assertThat(saga.reconciliar("t8").estado()).isEqualTo(Estado.PENDENTE);
        assertThat(saldoDaAna()).isEqualByComparingTo("70.00");
        assertThat(saga.reconciliar("t8").estado()).isEqualTo(Estado.CONCLUIDA);
    }

    @Test
    @DisplayName("iniciar de novo com o mesmo id (o cliente repetiu): não debita nem envia outra vez")
    void iniciarIdempotente() {
        parceiro.responde(Resposta.ACEITA);
        enviar30("t9");

        Transferencia repetida = enviar30("t9");

        assertThat(repetida.estado()).isEqualTo(Estado.CONCLUIDA);
        assertThat(saldoDaAna()).isEqualByComparingTo("70.00");
        assertThat(parceiro.recebidos).hasSize(1);
    }

    @Test
    @DisplayName("reconciliar o que já terminou não consulta o parceiro nem estorna de novo")
    void reconciliarTerminada() {
        parceiro.naoResponde().consultaDiz(Resposta.RECUSADA);
        enviar30("t10");
        saga.reconciliar("t10");

        assertThat(saga.reconciliar("t10").estado()).isEqualTo(Estado.COMPENSADA);
        assertThat(saldoDaAna()).as("estornado uma vez só").isEqualByComparingTo("100.00");
        assertThat(parceiro.chamadasDeConsulta).isEqualTo(1);
    }

    @Test
    @DisplayName("a saga guarda o estado: buscar devolve a transferência; id desconhecido na reconciliação é erro")
    void estadoGuardado() {
        parceiro.naoResponde();
        enviar30("t11");

        assertThat(saga.buscar("t11")).map(Transferencia::estado).contains(Estado.PENDENTE);
        assertThat(saga.buscar("nao-existe")).isEmpty();
        assertThatThrownBy(() -> saga.reconciliar("nao-existe")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("valor zero ou negativo é recusado antes de qualquer passo")
    void valorInvalido() {
        assertThatThrownBy(() -> saga.iniciar("t12", "ana", "x", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(saldoDaAna()).isEqualByComparingTo("100.00");
    }
}
