package dev.trilhajava.m15.documentos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Um produto do catálogo, como documento.
 *
 * TODO (15.02): declare os índices — sku único e (categoria, preco) composto.
 *
 * @param avaliacoes      só as 10 mais recentes
 * @param totalAvaliacoes quantas avaliações o produto já recebeu (todas, não só as 10)
 * @param somaDasNotas    para a média sem ler todas as avaliações
 */
@Document("produtos")
public record Produto(
        @Id String id,
        String sku,
        String nome,
        String categoria,
        BigDecimal preco,
        int estoque,
        Map<String, String> atributos,
        List<Avaliacao> avaliacoes,
        int totalAvaliacoes,
        int somaDasNotas) {

    public static Produto novo(String sku, String nome, String categoria, String preco, int estoque,
            Map<String, String> atributos) {
        return new Produto(null, sku, nome, categoria, new BigDecimal(preco), estoque, atributos, List.of(), 0, 0);
    }

    public double notaMedia() {
        return totalAvaliacoes == 0 ? 0 : (double) somaDasNotas / totalAvaliacoes;
    }
}
