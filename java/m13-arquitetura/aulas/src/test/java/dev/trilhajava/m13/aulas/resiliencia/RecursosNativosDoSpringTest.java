package dev.trilhajava.m13.aulas.resiliencia;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.resilience.InvocationRejectedException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/** @Retryable e @ConcurrencyLimit precisam do contexto do Spring: são aplicados por um proxy. */
class RecursosNativosDoSpringTest {

    private OperadoraFalsa operadora;
    private AnnotationConfigApplicationContext contexto;

    @BeforeEach
    void subir() {
        operadora = OperadoraFalsa.iniciar();
        var politica = PoliticaDeResiliencia.paraTestes();
        contexto = new AnnotationConfigApplicationContext();
        contexto.registerBean(PoliticaDeResiliencia.class, () -> politica);
        contexto.registerBean(ClienteDaOperadora.class, () -> new ClienteDaOperadora(
                operadora.url(), politica.timeoutDeConexao(), politica.timeoutDeLeitura()));
        contexto.register(ConfiguracaoDeResiliencia.class);
        contexto.refresh();
    }

    @AfterEach
    void derrubar() {
        contexto.close();
        operadora.close();
    }

    @Test
    void repetirComAMesmaChaveNaoCriaDuasAutorizacoes() {
        // A 1ª resposta demora mais que o timeout de leitura (300 ms): a operadora CRIOU a autorização,
        // mas o cliente viu só um timeout. A repetição reenvia a mesma chave.
        operadora.comAtrasoNasProximas(1, Duration.ofMillis(600));
        var autorizacao = contexto.getBean(AutorizacaoDeProcedimentos.class);

        String numero = autorizacao.solicitar("89001234", "ressonância de joelho", "pedido-medico-777");

        assertThat(numero).isEqualTo("AUT-1");
        assertThat(operadora.chamadasRecebidas()).isEqualTo(2);
        assertThat(operadora.autorizacoesCriadas()).isEqualTo(1);
    }

    @Test
    void acimaDoLimiteDeConcorrenciaAChamadaERecusadaNaHora() throws Exception {
        operadora.comAtraso(Duration.ofMillis(150));                 // lenta, mas dentro do timeout
        var consulta = contexto.getBean(ConsultaProtegidaDeElegibilidade.class);
        var largada = new CountDownLatch(1);

        List<Future<Elegibilidade>> resultados = new ArrayList<>();
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10; i++) {
                resultados.add(threads.submit(() -> {
                    largada.await();
                    return consulta.consultar("89001234");
                }));
            }
            largada.countDown();
        }

        int atendidas = 0;
        int recusadas = 0;
        for (Future<Elegibilidade> resultado : resultados) {
            try {
                resultado.get();
                atendidas++;
            } catch (ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(InvocationRejectedException.class);
                recusadas++;
            }
        }
        assertThat(atendidas).isEqualTo(4);
        assertThat(recusadas).isEqualTo(6);
    }
}
