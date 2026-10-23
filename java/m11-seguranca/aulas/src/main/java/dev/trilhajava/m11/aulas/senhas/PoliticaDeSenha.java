package dev.trilhajava.m11.aulas.senhas;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Política de senhas do portal, seguindo a NIST SP 800-63B rev. 4 para senha como fator único:
 * no mínimo 15 caracteres, pelo menos 64 aceitos, nenhuma regra de composição, e recusa de senhas
 * vazadas e de palavras do contexto (o nome do serviço e o usuário do e-mail).
 *
 * <p>O tamanho é contado em <em>code points</em> Unicode, como pede a NIST: "pão" tem 3
 * caracteres, e um emoji conta como 1, embora ocupe 2 {@code char}s em Java.
 */
public class PoliticaDeSenha {

    public enum Problema { CURTA, LONGA, VAZADA, PALAVRA_DO_CONTEXTO }

    static final int MINIMO = 15;
    static final int MAXIMO = 64;

    private final VerificadorDeSenhasVazadas vazadas;
    private final Set<String> palavrasDoServico;

    public PoliticaDeSenha(VerificadorDeSenhasVazadas vazadas, Set<String> palavrasDoServico) {
        this.vazadas = vazadas;
        this.palavrasDoServico = palavrasDoServico.stream()
                .map(p -> p.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Devolve todos os problemas de uma vez: a pessoa corrige tudo numa tentativa só. */
    public List<Problema> avaliar(String email, String senha) {
        List<Problema> problemas = new ArrayList<>();
        int tamanho = senha.codePointCount(0, senha.length());
        if (tamanho < MINIMO) {
            problemas.add(Problema.CURTA);
        }
        if (tamanho > MAXIMO) {
            problemas.add(Problema.LONGA);
        }
        if (contemPalavraDoContexto(email, senha)) {
            problemas.add(Problema.PALAVRA_DO_CONTEXTO);
        }
        // A consulta de vazadas pode ir à rede: só gasta essa viagem se o resto passou.
        if (problemas.isEmpty() && vazadas.vazada(senha)) {
            problemas.add(Problema.VAZADA);
        }
        return problemas;
    }

    private boolean contemPalavraDoContexto(String email, String senha) {
        String minuscula = senha.toLowerCase(Locale.ROOT);
        String usuario = email.substring(0, email.indexOf('@')).toLowerCase(Locale.ROOT);
        if (usuario.length() >= 4 && minuscula.contains(usuario)) {
            return true;
        }
        for (String palavra : palavrasDoServico) {
            if (minuscula.contains(palavra)) {
                return true;
            }
        }
        return false;
    }
}
