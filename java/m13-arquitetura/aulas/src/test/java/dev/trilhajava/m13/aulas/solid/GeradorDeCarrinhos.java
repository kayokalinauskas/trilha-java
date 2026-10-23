package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Gera carrinhos variados e reproduzíveis: a mesma semente produz os mesmos carrinhos. */
final class GeradorDeCarrinhos {

    private static final String[] CUPONS = {null, null, "PRIMEIRA10", "primeira10", "NATAL15", "ANIVERSARIO"};

    private GeradorDeCarrinhos() {
    }

    static Carrinho aleatorio(Random aleatorio) {
        TipoDeCliente tipo = TipoDeCliente.values()[aleatorio.nextInt(TipoDeCliente.values().length)];
        int quantidadeDeItens = 1 + aleatorio.nextInt(4);
        List<ItemDoPedido> itens = new ArrayList<>();
        for (int i = 0; i < quantidadeDeItens; i++) {
            int quantidade = 1 + aleatorio.nextInt(15);
            BigDecimal preco = BigDecimal.valueOf(50 + aleatorio.nextInt(60_000), 2);   // R$ 0,50 a R$ 600,49
            itens.add(new ItemDoPedido("SKU-" + i, quantidade, preco));
        }
        String cupom = CUPONS[aleatorio.nextInt(CUPONS.length)];
        return new Carrinho("cliente-" + aleatorio.nextInt(1_000), tipo, itens, cupom);
    }
}
