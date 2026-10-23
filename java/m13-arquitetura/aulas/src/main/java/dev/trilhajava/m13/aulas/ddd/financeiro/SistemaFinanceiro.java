package dev.trilhajava.m13.aulas.ddd.financeiro;

/** A API que o Financeiro oferece. Devolve null quando o cliente não tem ficha (o legado é assim). */
public interface SistemaFinanceiro {

    FichaFinanceira consultarFicha(String codCli);
}
