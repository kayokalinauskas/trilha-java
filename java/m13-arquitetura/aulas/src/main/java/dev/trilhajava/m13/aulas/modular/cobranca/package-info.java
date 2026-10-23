/**
 * Módulo cobranca: gera a cobrança de cada consulta realizada, autorizada pela operadora do
 * convênio. Da agenda, só pode usar os eventos.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Cobrança",
        allowedDependencies = "agenda :: eventos")
package dev.trilhajava.m13.aulas.modular.cobranca;
