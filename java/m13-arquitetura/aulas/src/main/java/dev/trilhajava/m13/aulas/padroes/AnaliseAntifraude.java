package dev.trilhajava.m13.aulas.padroes;

import java.util.List;
import java.util.Optional;

/**
 * Chain of Responsibility na forma moderna: uma lista ordenada de regras, avaliada até a
 * primeira que recusar. As regras não se conhecem, e a ordem fica num lugar só.
 */
public class AnaliseAntifraude {

    private final List<RegraAntifraude> regras;

    public AnaliseAntifraude(List<RegraAntifraude> regras) {
        this.regras = List.copyOf(regras);
    }

    public Optional<String> avaliar(SolicitacaoDeCobranca solicitacao) {
        for (RegraAntifraude regra : regras) {
            Optional<String> recusa = regra.avaliar(solicitacao);
            if (recusa.isPresent()) {
                return recusa; // curto-circuito: as regras seguintes nem rodam
            }
        }
        return Optional.empty();
    }
}
