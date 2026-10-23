package dev.trilhajava.m06;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/**
 * Solução de referência do exercício 06.04.
 *
 * Virtual threads são baratas: criamos UMA POR TAREFA (não se faz pool delas). O que é escasso aqui
 * é o serviço externo, então o limite vem de um Semaphore, e não do tamanho de um pool de threads.
 */
public class ProcessadorDeNotas {

    public ResultadoLote processar(List<String> idsPedidos, Function<String, String> emitirNota, int maxSimultaneas) {
        if (maxSimultaneas < 1) {
            throw new IllegalArgumentException("maxSimultaneas deve ser pelo menos 1");
        }
        Semaphore permissoes = new Semaphore(maxSimultaneas);
        Map<String, Future<String>> futuros = new LinkedHashMap<>(); // preserva a ordem dos ids

        // try-with-resources: close() espera todas as tarefas terminarem (Java 19+).
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String id : idsPedidos) {
                futuros.put(id, executor.submit(() -> {
                    permissoes.acquire();
                    try {
                        return emitirNota.apply(id);
                    } finally {
                        permissoes.release();
                    }
                }));
            }
        }

        Map<String, String> sucessos = new LinkedHashMap<>();
        Map<String, String> falhas = new LinkedHashMap<>();
        futuros.forEach((id, futuro) -> {
            try {
                sucessos.put(id, futuro.get());
            } catch (ExecutionException e) {
                falhas.put(id, e.getCause().getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                falhas.put(id, "interrompido");
            }
        });
        return new ResultadoLote(sucessos, falhas);
    }
}
