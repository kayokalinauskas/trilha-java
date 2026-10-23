package dev.trilhajava.m10.aulas.fatias;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O caso de uso. Nos testes de controller ele vira um {@code @MockitoBean}; nos testes de
 * repositório ele nem existe; no {@code @SpringBootTest} ele roda de verdade.
 */
@Service
public class CatalogoDeProdutos {

    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;

    public CatalogoDeProdutos(ProdutoRepository produtos, CategoriaRepository categorias) {
        this.produtos = produtos;
        this.categorias = categorias;
    }

    @Transactional
    public ProdutoResposta cadastrar(NovoProduto novo) {
        Categoria categoria = categorias.findByNome(novo.categoria())
                .orElseGet(() -> categorias.save(new Categoria(novo.categoria())));
        Produto produto = produtos.save(
                new Produto(novo.sku(), novo.nome(), novo.preco(), novo.quantidadeInicial(), categoria));
        return ProdutoResposta.de(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoResposta buscar(String sku) {
        return produtos.findBySku(sku)
                .map(ProdutoResposta::de)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(sku));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResposta> listarAtivos() {
        return produtos.listarAtivosComCategoria().stream().map(ProdutoResposta::de).toList();
    }

    /** Tira do estoque e devolve o saldo. A alteração vai para o banco no commit (dirty checking). */
    @Transactional
    public int baixarEstoque(String sku, int quantidade) {
        Produto produto = produtos.findBySku(sku).orElseThrow(() -> new ProdutoNaoEncontradoException(sku));
        produto.baixar(quantidade);
        return produto.getQuantidadeEmEstoque();
    }
}
