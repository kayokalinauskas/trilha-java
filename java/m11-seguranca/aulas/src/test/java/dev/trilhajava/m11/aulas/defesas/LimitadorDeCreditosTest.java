package dev.trilhajava.m11.aulas.defesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LimitadorDeCreditosTest {

    // 6 créditos por minuto: 1 a cada 10 segundos, rajada de até 6.
    static final PoliticaDeLimite SEIS_POR_MINUTO = new PoliticaDeLimite("por-cliente", 6, Duration.ofMinutes(1));

    RelogioAjustavel relogio;
    LimitadorDeCreditos limitador;

    @BeforeEach
    void preparar() {
        relogio = new RelogioAjustavel(Instant.parse("2026-10-05T12:00:00Z"));
        limitador = new LimitadorDeCreditos(SEIS_POR_MINUTO, relogio);
    }

    @Test
    void comecaCheioEAceitaUmaRajadaDoTamanhoDaCapacidade() {
        for (int esperado = 5; esperado >= 0; esperado--) {
            Consumo consumo = limitador.consumir("ana", 1);
            assertThat(consumo.permitido()).isTrue();
            assertThat(consumo.restantes()).isEqualTo(esperado);
        }

        Consumo setima = limitador.consumir("ana", 1);

        assertThat(setima.permitido()).isFalse();
        assertThat(setima.restantes()).isZero();
        assertThat(setima.tentarDepoisDe()).isEqualTo(Duration.ofSeconds(10));
        assertThat(setima.ateEncher()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void recusaNaoGastaEOTempoQueFaltaDiminui() {
        esvaziar("ana");

        relogio.avancar(Duration.ofSeconds(4));
        Consumo recusada = limitador.consumir("ana", 1);
        assertThat(recusada.permitido()).isFalse();
        assertThat(recusada.tentarDepoisDe()).isEqualTo(Duration.ofSeconds(6));

        relogio.avancar(Duration.ofSeconds(6));
        assertThat(limitador.consumir("ana", 1).permitido()).isTrue();
    }

    @Test
    void reposicaoContinuaEFracoesSeAcumulam() {
        esvaziar("ana");

        relogio.avancar(Duration.ofSeconds(5));
        assertThat(limitador.disponiveis("ana")).isZero();
        relogio.avancar(Duration.ofSeconds(5));                 // 5 s + 5 s = 1 crédito
        assertThat(limitador.disponiveis("ana")).isEqualTo(1);
        relogio.avancar(Duration.ofSeconds(15));                // 25 s no total
        assertThat(limitador.disponiveis("ana")).isEqualTo(2);
    }

    @Test
    void nuncaPassaDaCapacidade() {
        limitador.consumir("ana", 1);
        relogio.avancar(Duration.ofHours(3));

        assertThat(limitador.disponiveis("ana")).isEqualTo(6);
    }

    @Test
    void custoMaiorQueOSaldoERecusadoMesmoComSaldoPositivo() {
        for (int i = 0; i < 4; i++) {
            limitador.consumir("ana", 1);
        }

        Consumo exportacao = limitador.consumir("ana", 3);      // sobram 2, a exportação custa 3

        assertThat(exportacao.permitido()).isFalse();
        assertThat(exportacao.restantes()).isEqualTo(2);
        assertThat(exportacao.tentarDepoisDe()).isEqualTo(Duration.ofSeconds(10));
        assertThat(limitador.consumir("ana", 1).permitido()).isTrue();   // a barata ainda cabe
    }

    @Test
    void custoForaDosLimitesEErroDeProgramacao() {
        assertThatThrownBy(() -> limitador.consumir("ana", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("custo deve estar entre 1 e 6: 0");
        assertThatThrownBy(() -> limitador.consumir("ana", 7))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void politicaInvalidaNaoSobe() {
        assertThatThrownBy(() -> new PoliticaDeLimite("por-cliente", 0, Duration.ofMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("capacidade deve ser positiva: 0");
        assertThatThrownBy(() -> new PoliticaDeLimite("por-cliente", 10, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void relogioQueVoltaNuncaCriaCreditos() {
        for (int i = 0; i < 3; i++) {
            limitador.consumir("ana", 1);
        }
        assertThat(limitador.disponiveis("ana")).isEqualTo(3);

        relogio.voltar(Duration.ofSeconds(30));                 // ajuste de NTP para trás
        assertThat(limitador.disponiveis("ana")).isZero();      // pune por um tempo...

        relogio.avancar(Duration.ofSeconds(30));
        assertThat(limitador.disponiveis("ana")).isEqualTo(3);  // ...e volta ao que era, sem ganhar nada
    }

    @Test
    void cadaChaveTemOSeuBalde() {
        esvaziar("ana");

        assertThat(limitador.consumir("bruno", 1).permitido()).isTrue();
        assertThat(limitador.consumir("ana", 1).permitido()).isFalse();
    }

    @Test
    void baldesCheiosSaemDaMemoria() {
        limitador.consumir("ana", 1);
        limitador.consumir("bruno", 1);
        relogio.avancar(Duration.ofMinutes(1));
        limitador.consumir("carla", 1);                         // ainda não encheu

        assertThat(limitador.removerOciosos()).isEqualTo(2);
        assertThat(limitador.chavesEmMemoria()).isEqualTo(1);
        assertThat(limitador.disponiveis("ana")).isEqualTo(6);  // ausente = cheio
    }

    @Test
    void cemThreadsDisputandoCinquentaCreditos() throws Exception {
        var limitadorGrande = new LimitadorDeCreditos(
                new PoliticaDeLimite("por-cliente", 50, Duration.ofMinutes(1)), relogio);
        var largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100; i++) {
                resultados.add(executor.submit(() -> {
                    largada.await();
                    return limitadorGrande.consumir("ana", 1).permitido();
                }));
            }
            largada.countDown();
        }

        long permitidas = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get()) {
                permitidas++;
            }
        }
        assertThat(permitidas).isEqualTo(50);
    }

    private void esvaziar(String chave) {
        while (limitador.consumir(chave, 1).permitido()) {
            // gasta até recusar
        }
    }
}
