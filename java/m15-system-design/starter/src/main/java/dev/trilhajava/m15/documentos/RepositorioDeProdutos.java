package dev.trilhajava.m15.documentos;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioDeProdutos extends MongoRepository<Produto, String> {

    Optional<Produto> findBySku(String sku);

    // TODO (15.02): a consulta por categoria e faixa de preço, paginada
}
