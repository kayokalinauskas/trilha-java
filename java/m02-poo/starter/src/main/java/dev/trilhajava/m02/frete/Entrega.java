package dev.trilhajava.m02.frete;

/**
 * Exercício 02.04 — as modalidades de entrega formam uma hierarquia FECHADA:
 * o compilador conhece todas as implementações, então um switch sobre Entrega
 * pode ser exaustivo sem "default".
 *
 * Os records já estão declarados. Seu trabalho: validar em cada construtor compacto
 * e implementar a CalculadoraFrete.
 */
public sealed interface Entrega permits Retirada, EntregaPadrao, EntregaExpressa, EntregaAgendada {
}
