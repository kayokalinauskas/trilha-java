package dev.trilhajava.m13.aulas.ddd.financeiro;

/**
 * O modelo do contexto Financeiro (um sistema legado), do jeito que ele expõe: nomes herdados
 * das colunas, valores em centavos e a situação codificada numa letra ("A" ativo, "B" bloqueado, "I" inadimplente).
 * O contexto de Vendas não deve usar este tipo fora da ACL.
 */
public record FichaFinanceira(String codCli, long vlLimCredCentavos, long vlEmAbertoCentavos, String sitCli) {
}
