package dev.trilhajava.m09.imoveis;

import org.springframework.data.domain.Page;

/**
 * Exercício 09.04 — busca de imóveis com filtros opcionais, paginação e ordenação.
 *
 * Ordenações aceitas (qualquer outra → IllegalArgumentException; null = "recentes"):
 *   "recentes" (publicadoEm desc), "menor-preco", "maior-preco", "maior-area" — empates por id crescente.
 * Página >= 0; tamanho entre 1 e 50 (senão, IllegalArgumentException).
 *
 * Dica: ImovelRepository estendendo JpaSpecificationExecutor<Imovel> e uma Specification por filtro.
 */
public class BuscaDeImoveis {

    public Page<ImovelResumo> buscar(FiltroDeImoveis filtro, int pagina, int tamanho, String ordenacao) {
        throw new UnsupportedOperationException("TODO");
    }
}
