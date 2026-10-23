package dev.trilhajava.m09.imoveis;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Filtros da busca. Qualquer campo nulo (ou conjunto vazio) significa "não filtrar por isso".
 *
 * @param cidade     igual, sem diferenciar maiúsculas/minúsculas
 * @param comGaragem true = pelo menos 1 vaga; false = nenhuma vaga
 * @param bairros    o imóvel está em um destes bairros
 */
public record FiltroDeImoveis(String cidade, TipoImovel tipo, BigDecimal precoMin, BigDecimal precoMax,
        Integer quartosMin, Boolean comGaragem, Set<String> bairros) {

    public static FiltroDeImoveis vazio() {
        return new FiltroDeImoveis(null, null, null, null, null, null, null);
    }
}
