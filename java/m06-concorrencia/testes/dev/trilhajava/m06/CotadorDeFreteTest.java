package dev.trilhajava.m06;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 06.03 — Cotação de frete paralela com timeout")
class CotadorDeFreteTest {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @AfterEach
    void fechar() {
        executor.shutdownNow(); // interrompe a "lerda" em vez de esperar os 5 s dela
    }

    private static Transportadora fake(String nome, long demoraMs, String preco) {
        return new Transportadora() {
            public String nome() {
                return nome;
            }

            public BigDecimal cotar(String cep, double peso) {
                try {
                    Thread.sleep(demoraMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("interrompida", e);
                }
                if (preco == null) {
                    throw new IllegalStateException(nome + " fora do ar");
                }
                return new BigDecimal(preco);
            }
        };
    }

    @Test
    @DisplayName("consulta em paralelo: 5 transportadoras de 300 ms levam ~300 ms, não 1,5 s")
    void paralelo() {
        List<Transportadora> ts = List.of(fake("A", 300, "30"), fake("B", 300, "25"), fake("C", 300, "40"),
                fake("D", 300, "28"), fake("E", 300, "33"));
        CotadorDeFrete cotador = new CotadorDeFrete(ts, executor, Duration.ofSeconds(2));
        long inicio = System.nanoTime();
        List<Cotacao> r = cotador.cotarTodas("01310100", 2.0);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertThat(r).extracting(Cotacao::transportadora).containsExactly("B", "D", "A", "E", "C");
        assertThat(ms).isLessThan(1_000);
    }

    @Test
    @DisplayName("falhas e lentidão acima do timeout são ignoradas; o resto chega")
    void resiliente() {
        List<Transportadora> ts = List.of(fake("Rápida", 50, "30"), fake("Quebrada", 10, null), fake("Lerda", 5_000, "1"));
        CotadorDeFrete cotador = new CotadorDeFrete(ts, executor, Duration.ofMillis(500));
        long inicio = System.nanoTime();
        List<Cotacao> r = cotador.cotarTodas("01310100", 2.0);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertThat(r).extracting(Cotacao::transportadora).containsExactly("Rápida");
        assertThat(ms).as("não espera a lerda").isLessThan(2_000);
    }

    @Test
    @DisplayName("mais barata, ou vazio se ninguém respondeu")
    void maisBarata() {
        CotadorDeFrete cotador = new CotadorDeFrete(List.of(fake("A", 10, "19.90"), fake("B", 10, "18.00")), executor, Duration.ofSeconds(1));
        assertThat(cotador.maisBarata("01310100", 1.0)).map(Cotacao::transportadora).contains("B");
        CotadorDeFrete ninguem = new CotadorDeFrete(List.of(fake("X", 10, null)), executor, Duration.ofSeconds(1));
        assertThat(ninguem.maisBarata("01310100", 1.0)).isEmpty();
    }
}
