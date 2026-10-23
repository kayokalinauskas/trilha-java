package dev.trilhajava.m03.catalogo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Solução de referência do exercício 03.01.
 *
 * Um TreeMap indexado pelo código resolve três problemas de uma vez:
 * deduplicação (chave única), desempate (merge) e ordenação final (TreeMap é ordenado pela chave).
 */
public class ConsolidadorCatalogo {

    public ResultadoConsolidacao consolidar(Collection<RegistroPeca> legado, Collection<RegistroPeca> novo) {
        Map<String, Peca> porCodigo = new TreeMap<>();
        int rejeitados = 0;

        // A ordem importa: processando o sistema novo por último, ele vence nos empates.
        for (Collection<RegistroPeca> fonte : List.of(legado, novo)) {
            for (RegistroPeca registro : fonte) {
                Peca peca = converter(registro);
                if (peca == null) {
                    rejeitados++;
                    continue;
                }
                porCodigo.merge(peca.codigo(), peca, ConsolidadorCatalogo::maisRecente);
            }
        }
        return new ResultadoConsolidacao(new ArrayList<>(porCodigo.values()), rejeitados);
    }

    /** Em empate de data, fica a que chegou depois (a "candidata"). */
    private static Peca maisRecente(Peca atual, Peca candidata) {
        return candidata.atualizadoEm().isBefore(atual.atualizadoEm()) ? atual : candidata;
    }

    /** Converte o registro cru em Peca; devolve null se o registro for inválido. */
    private static Peca converter(RegistroPeca r) {
        if (r == null || r.codigo() == null || r.codigo().isBlank()
                || r.nome() == null || r.preco() == null || r.atualizadoEm() == null) {
            return null;
        }
        return new Peca(r.codigo(), r.nome(), r.preco(), r.atualizadoEm());
    }
}
