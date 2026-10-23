package dev.trilhajava.m13.aulas.padroes;

import java.util.Optional;

/** Um elo da cadeia de antifraude: devolve o motivo da recusa, ou vazio para deixar passar. */
@FunctionalInterface
public interface RegraAntifraude {

    Optional<String> avaliar(SolicitacaoDeCobranca solicitacao);
}
