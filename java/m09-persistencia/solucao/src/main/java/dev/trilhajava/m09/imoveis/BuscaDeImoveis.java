package dev.trilhajava.m09.imoveis;

import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuscaDeImoveis {

    /**
     * Lista branca de ordenações: o cliente escolhe um NOME, nunca uma coluna. Aceitar "sort=qualquerCampo"
     * direto do request expõe colunas internas e abre espaço para consultas caras ou injeção.
     * O id no fim garante ordem estável entre páginas (empates de preço não "embaralham" a paginação).
     */
    private static final Map<String, Sort> ORDENACOES = Map.of(
            "recentes", Sort.by(Sort.Order.desc("publicadoEm"), Sort.Order.asc("id")),
            "menor-preco", Sort.by(Sort.Order.asc("preco"), Sort.Order.asc("id")),
            "maior-preco", Sort.by(Sort.Order.desc("preco"), Sort.Order.asc("id")),
            "maior-area", Sort.by(Sort.Order.desc("areaM2"), Sort.Order.asc("id")));

    private final ImovelRepository imoveis;

    public BuscaDeImoveis(ImovelRepository imoveis) {
        this.imoveis = imoveis;
    }

    @Transactional(readOnly = true)
    public Page<ImovelResumo> buscar(FiltroDeImoveis filtro, int pagina, int tamanho, String ordenacao) {
        if (pagina < 0 || tamanho < 1 || tamanho > 50) {
            throw new IllegalArgumentException("Página deve ser >= 0 e tamanho entre 1 e 50");
        }
        Sort ordem = ORDENACOES.get(ordenacao == null ? "recentes" : ordenacao);
        if (ordem == null) {
            throw new IllegalArgumentException("Ordenação inválida: " + ordenacao + ". Use " + ORDENACOES.keySet());
        }
        FiltroDeImoveis f = filtro == null ? FiltroDeImoveis.vazio() : filtro;
        return imoveis.findAll(EspecificacoesDeImovel.de(f), PageRequest.of(pagina, tamanho, ordem))
                .map(i -> new ImovelResumo(i.getId(), i.getTitulo(), i.getCidade(), i.getBairro(), i.getPreco(),
                        i.getQuartos()));
    }
}
