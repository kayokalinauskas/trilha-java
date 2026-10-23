package dev.trilhajava.m15.documentos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Um produto é um documento: os atributos variam por categoria (uma camiseta tem cor e tamanho; um
 * notebook, memória e tela), sem tabela de atributos nem colunas vazias. As avaliações recentes vêm
 * embutidas, e os totais ficam pré-calculados no próprio documento.
 *
 * @param avaliacoes      só as 10 mais recentes (um array que cresce sem limite deixa o documento enorme)
 * @param totalAvaliacoes quantas avaliações o produto já recebeu (todas, não só as 10)
 * @param somaDasNotas    para a média sem ler todas as avaliações
 */
@Document("produtos")
@CompoundIndex(name = "categoria_preco", def = "{'categoria': 1, 'preco': 1}")
public record Produto(
        @Id String id,
        @Indexed(unique = true) String sku,
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
