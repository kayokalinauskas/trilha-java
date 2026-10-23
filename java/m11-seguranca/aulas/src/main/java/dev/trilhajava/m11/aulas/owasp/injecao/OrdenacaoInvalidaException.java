package dev.trilhajava.m11.aulas.owasp.injecao;

import java.util.Set;
import java.util.TreeSet;

/** Campo de ordenação fora da allowlist. A mensagem lista só os nomes públicos, nunca colunas internas. */
public class OrdenacaoInvalidaException extends RuntimeException {

    public OrdenacaoInvalidaException(String campo, Set<String> permitidos) {
        super("Não é possível ordenar por '" + resumir(campo) + "'. Use um destes: " + new TreeSet<>(permitidos));
    }

    private static String resumir(String campo) {
        return campo.length() > 30 ? campo.substring(0, 30) + "…" : campo;
    }
}
