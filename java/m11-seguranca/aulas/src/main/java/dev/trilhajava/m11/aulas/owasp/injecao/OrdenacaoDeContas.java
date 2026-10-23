package dev.trilhajava.m11.aulas.owasp.injecao;

import java.util.Map;

/**
 * Allowlist de ordenação: o cliente escolhe um NOME público ("saldo", "-saldo"),
 * e o código traduz para um trecho de SQL escrito por nós. Texto do cliente nunca vira SQL.
 */
public final class OrdenacaoDeContas {

    private static final Map<String, String> COLUNAS = Map.of(
            "titular", "titular",
            "saldo", "saldo",
            "abertura", "id");

    private OrdenacaoDeContas() {
    }

    /**
     * Converte "saldo" em "saldo ASC" e "-saldo" em "saldo DESC".
     *
     * @throws OrdenacaoInvalidaException se o campo não estiver na lista (vira 400 na API)
     */
    public static String paraSql(String pedido) {
        if (pedido == null || pedido.isBlank()) {
            return "id ASC";
        }
        boolean decrescente = pedido.startsWith("-");
        String campo = decrescente ? pedido.substring(1) : pedido;
        String coluna = COLUNAS.get(campo);
        if (coluna == null) {
            throw new OrdenacaoInvalidaException(campo, COLUNAS.keySet());
        }
        // desempate por id: a paginação fica estável mesmo com saldos iguais
        return coluna + (decrescente ? " DESC" : " ASC") + ", id ASC";
    }
}
