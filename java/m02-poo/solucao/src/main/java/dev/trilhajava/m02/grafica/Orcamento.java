package dev.trilhajava.m02.grafica;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Agrega itens. Não conhece nenhuma forma concreta nem nenhuma regra de preço. */
public final class Orcamento {

    private final List<ItemOrcamento> itens = new ArrayList<>();

    public void adicionar(Forma forma, int quantidade) {
        itens.add(new ItemOrcamento(forma, quantidade)); // a validação mora no ItemOrcamento
    }

    public List<ItemOrcamento> itens() {
        return List.copyOf(itens);
    }

    public double areaTotal() {
        double total = 0;
        for (ItemOrcamento item : itens) {
            total += item.areaTotal(); // polimorfismo: nenhum instanceof aqui
        }
        return total;
    }

    public BigDecimal valorTotal(Precificacao precificacao) {
        return precificacao.precoPara(areaTotal());
    }
}
