package dev.trilhajava.m10.aulas.desempenho;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simula a API HTTP da transportadora, que leva cerca de 40 ms para informar o prazo.
 * Num sistema real, aqui haveria um RestClient; para a aula, o que importa é a espera.
 */
@Component
public class Transportadora {

    private final Duration latencia;

    public Transportadora(@Value("${frete.transportadora.latencia:40ms}") Duration latencia) {
        this.latencia = latencia;
    }

    public int prazoEmDiasUteis(String regiao) {
        try {
            Thread.sleep(latencia);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Consulta à transportadora interrompida", e);
        }
        return switch (regiao) {
            case "Capital SP" -> 1;
            case "Interior SP" -> 2;
            case "Sul" -> 4;
            default -> 6;
        };
    }
}
