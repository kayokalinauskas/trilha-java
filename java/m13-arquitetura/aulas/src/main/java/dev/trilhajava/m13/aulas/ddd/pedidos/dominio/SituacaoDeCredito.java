package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.util.Objects;

/** O crédito do cliente como o contexto de Vendas precisa enxergar: nos termos de Vendas, não do Financeiro. */
public record SituacaoDeCredito(Dinheiro limite, Dinheiro emAberto, boolean bloqueado) {

    public SituacaoDeCredito {
        Objects.requireNonNull(limite, "limite");
        Objects.requireNonNull(emAberto, "emAberto");
    }

    public Dinheiro disponivel() {
        return limite.menosAteZero(emAberto);
    }
}
