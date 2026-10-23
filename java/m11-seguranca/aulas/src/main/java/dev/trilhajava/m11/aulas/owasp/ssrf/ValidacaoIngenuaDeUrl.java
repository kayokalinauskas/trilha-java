package dev.trilhajava.m11.aulas.owasp.ssrf;

/**
 * VULNERÁVEL DE PROPÓSITO (A01, CWE-918 SSRF). Não copie.
 * Uma tentativa comum de proteção: comparar o TEXTO da URL com o domínio do parceiro.
 */
public final class ValidacaoIngenuaDeUrl {

    private ValidacaoIngenuaDeUrl() {
    }

    public static boolean permitida(String url) {
        return url.startsWith("https://") && url.contains("parceiro.com.br");
    }
}
