package dev.trilhajava.m10.aulas.fatias;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Consultas derivadas: o Spring Data monta o JPQL a partir do nome do método.
    Optional<Produto> findBySku(String sku);

    List<Produto> findByAtivoTrueAndQuantidadeEmEstoqueLessThanOrderBySku(int limite);

    List<Produto> findByAtivoTrueOrderByNome();

    // Consultas escritas à mão: o JPQL é validado quando o contexto sobe.
    @Query("""
            select p from Produto p
              join fetch p.categoria
             where p.ativo = true
             order by p.nome""")
    List<Produto> listarAtivosComCategoria();

    @Query("""
            select new dev.trilhajava.m10.aulas.fatias.ValorPorCategoria(c.nome, sum(p.preco * p.quantidadeEmEstoque))
              from Produto p join p.categoria c
             where p.ativo = true
             group by c.nome
             order by c.nome""")
    List<ValorPorCategoria> valorEmEstoquePorCategoria();
}
