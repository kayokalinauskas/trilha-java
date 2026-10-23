package dev.trilhajava.m12.aulas.esquemas;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * O evento "pedido confirmado" na versão 2, do ponto de vista do serviço de frete (consumidor).
 *
 * <p>É um <b>leitor tolerante</b>: declara só os campos que o frete usa, ignora os que não
 * conhece, aceita o nome antigo do CEP enquanto o produtor faz a migração e tem um valor
 * padrão para a forma de pagamento quando o campo não vem.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoConfirmado(
        String pedidoId,
        String clienteId,
        ValorTotal valorTotal,
        @JsonAlias("cepDestino") String cepDeEntrega,
        FormaDePagamento formaDePagamento) {

    public PedidoConfirmado {
        Objects.requireNonNull(pedidoId, "pedidoId");
        Objects.requireNonNull(valorTotal, "valorTotal");
        Objects.requireNonNull(cepDeEntrega, "cepDeEntrega");
        formaDePagamento = formaDePagamento == null ? FormaDePagamento.DESCONHECIDA : formaDePagamento;
    }
}
