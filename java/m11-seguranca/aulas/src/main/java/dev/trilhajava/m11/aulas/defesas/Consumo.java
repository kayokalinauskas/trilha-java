package dev.trilhajava.m11.aulas.defesas;

import java.time.Duration;

/**
 * O resultado de uma tentativa de gastar créditos.
 *
 * @param permitido      se a requisição pode seguir
 * @param restantes      créditos inteiros disponíveis DEPOIS desta tentativa
 * @param tentarDepoisDe quanto esperar para que esta MESMA tentativa caiba (zero se permitida)
 * @param ateEncher      quanto falta para o balde ficar cheio de novo (zero se já está cheio)
 */
public record Consumo(boolean permitido, int restantes, Duration tentarDepoisDe, Duration ateEncher) {

    /** Segundos inteiros, arredondados para cima: o formato de Retry-After e do parâmetro t de RateLimit. */
    public static long emSegundos(Duration duracao) {
        long segundos = duracao.getSeconds();
        return duracao.getNano() > 0 ? segundos + 1 : segundos;
    }
}
