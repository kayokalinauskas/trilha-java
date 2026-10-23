package dev.trilhajava.m11.limite;

import java.time.Duration;

/**
 * Resposta do limitador para uma tentativa.
 *
 * @param permitida      a requisição pode seguir?
 * @param restantes      fichas que sobraram DEPOIS desta tentativa
 * @param tentarDepoisDe quanto esperar até haver uma ficha ({@link Duration#ZERO} se já há)
 */
public record Decisao(boolean permitida, int restantes, Duration tentarDepoisDe) {}
