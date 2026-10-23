package dev.trilhajava.m13.aulas.resiliencia;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * A falha em cascata, ao vivo. Oito threads fazem o papel das threads do Tomcat; chegam 24 requisições
 * ao mesmo tempo: 16 conferem o convênio (a operadora está levando 2 s) e 8 só abrem a agenda (10 ms).
 * Rodada 1: sem defesa. Rodada 2: timeout de leitura de 300 ms e no máximo 2 consultas simultâneas.
 *
 * <pre>mvn -q compile exec:java -Dexec.mainClass=dev.trilhajava.m13.aulas.resiliencia.DemoDaCascata</pre>
 */
public final class DemoDaCascata {

    public static void main(String[] args) throws Exception {
        Logs.soAvisosDasBibliotecas();
        try (var operadora = OperadoraFalsa.iniciar().comAtraso(Duration.ofSeconds(2))) {
            rodada("Sem defesa (timeout de leitura de 30 s, sem limite)", operadora, Duration.ofSeconds(30), 16);
            rodada("Timeout de 300 ms e no máximo 2 consultas simultâneas", operadora, Duration.ofMillis(300), 2);
        }
    }

    private static void rodada(String titulo, OperadoraFalsa operadora, Duration timeoutDeLeitura, int vagas)
            throws Exception {
        var cliente = new ClienteDaOperadora(operadora.url(), Duration.ofMillis(500), timeoutDeLeitura);
        var compartimento = new Semaphore(vagas);              // o bulkhead, à mão (o @ConcurrencyLimit faz isto)
        ExecutorService threadsDoTomcat = Executors.newFixedThreadPool(8);
        long inicio = System.nanoTime();

        List<Future<Long>> agenda = new ArrayList<>();
        List<Future<String>> convenio = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (i % 3 == 2) {
                agenda.add(threadsDoTomcat.submit(() -> {
                    Thread.sleep(10);                            // abrir a agenda: rápido e sem dependência
                    return milissegundosDesde(inicio);
                }));
            } else {
                String carteirinha = "8900" + i;
                convenio.add(threadsDoTomcat.submit(() -> {
                    if (!compartimento.tryAcquire()) {
                        return "recusada na hora";
                    }
                    try {
                        cliente.consultarElegibilidade(carteirinha);
                        return "confirmada";
                    } catch (OperadoraIndisponivelException e) {
                        return "timeout";
                    } finally {
                        compartimento.release();
                    }
                }));
            }
        }

        long agendaMaisLenta = 0;
        for (Future<Long> f : agenda) {
            agendaMaisLenta = Math.max(agendaMaisLenta, f.get());
        }
        var resultados = new TreeMap<String, Integer>();
        for (Future<String> f : convenio) {
            resultados.merge(f.get(), 1, Integer::sum);
        }
        threadsDoTomcat.shutdown();

        System.out.println("== " + titulo);
        System.out.printf("   agenda: 8 requisições; a última terminou em %d ms%n", agendaMaisLenta);
        System.out.printf("   convênio: %s; tudo terminou em %d ms%n", resultados, milissegundosDesde(inicio));
    }

    private static long milissegundosDesde(long inicio) {
        return Duration.ofNanos(System.nanoTime() - inicio).toMillis();
    }
}
