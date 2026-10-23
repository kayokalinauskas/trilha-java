package dev.trilhajava.m13.aulas.ddd.pedidos.aplicacao;

import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.ClienteId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.SituacaoDeCredito;

/** Porta de saída: Vendas pergunta pelo crédito nos próprios termos. Quem traduz do Financeiro é o adaptador (a ACL). */
public interface ConsultaDeCredito {

    SituacaoDeCredito situacaoDe(ClienteId cliente);
}
