package dev.trilhajava.m10.aulas.fatias;

import java.math.BigDecimal;

/** O JSON que a API devolve. Nunca expomos a entidade: o contrato HTTP não muda quando o mapeamento muda. */
public record ProdutoResposta(String sku, String nome, BigDecimal preco, int quantidadeEmEstoque, String categoria) {

    static ProdutoResposta de(Produto produto) {
        return new ProdutoResposta(produto.getSku(), produto.getNome(), produto.getPreco(),
                produto.getQuantidadeEmEstoque(), produto.getCategoria().getNome());
    }
}
