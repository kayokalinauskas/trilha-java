package dev.trilhajava.m13.aulas.ddd.pedidos.adaptador;

import dev.trilhajava.m13.aulas.ddd.financeiro.FichaFinanceira;
import dev.trilhajava.m13.aulas.ddd.financeiro.SistemaFinanceiro;
import dev.trilhajava.m13.aulas.ddd.pedidos.aplicacao.ConsultaDeCredito;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.ClienteId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Dinheiro;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.SituacaoDeCredito;

/**
 * Camada anticorrupção (ACL): o único lugar de Vendas que conhece o modelo do Financeiro.
 * Traduz centavos, letras e null para a SituacaoDeCredito, e falha alto diante do que não entende.
 */
public final class CreditoViaFinanceiro implements ConsultaDeCredito {

    private final SistemaFinanceiro financeiro;

    public CreditoViaFinanceiro(SistemaFinanceiro financeiro) {
        this.financeiro = financeiro;
    }

    @Override
    public SituacaoDeCredito situacaoDe(ClienteId cliente) {
        FichaFinanceira ficha = financeiro.consultarFicha(cliente.valor());
        if (ficha == null) {
            return new SituacaoDeCredito(Dinheiro.ZERO, Dinheiro.ZERO, true);   // sem ficha, sem crédito
        }
        boolean bloqueado = switch (ficha.sitCli()) {
            case "A" -> false;
            case "B", "I" -> true;
            default -> throw new IllegalStateException(
                    "Situação desconhecida no Financeiro para o cliente " + cliente + ": " + ficha.sitCli());
        };
        return new SituacaoDeCredito(
                Dinheiro.deCentavos(ficha.vlLimCredCentavos()),
                Dinheiro.deCentavos(ficha.vlEmAbertoCentavos()),
                bloqueado);
    }
}
