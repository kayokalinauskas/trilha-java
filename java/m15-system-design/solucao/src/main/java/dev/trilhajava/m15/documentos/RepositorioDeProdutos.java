package dev.trilhajava.m15.documentos;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioDeProdutos extends MongoRepository<Produto, String> {

    Optional<Produto> findBySku(String sku);

    /**
     * Usa o índice composto (categoria, preco): igualdade primeiro, faixa depois. Atenção: com dois
     * parâmetros (minimo, maximo), o Between do Spring Data MongoDB é EXCLUSIVO nas duas pontas ($gt/$lt).
     * Com um Range, quem chama decide: Range.closed(9.90, 100.00) vira $gte/$lte.
     */
    Page<Produto> findByCategoriaAndPrecoBetween(String categoria, Range<BigDecimal> faixa, Pageable pagina);
}
