package dev.trilhajava.m11.aulas.owasp.desserializacao;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A versão corrigida do polimorfismo: o JSON traz um NOME lógico ("pix", "cartao"), e só os
 * subtipos listados aqui podem nascer. Nome de classe Java nunca passa pela rede.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MeioDePagamento.Pix.class, name = "pix"),
        @JsonSubTypes.Type(value = MeioDePagamento.Cartao.class, name = "cartao")})
public sealed interface MeioDePagamento {

    BigDecimal valor();

    record Pix(String chave, BigDecimal valor) implements MeioDePagamento {
    }

    record Cartao(String ultimosDigitos, int parcelas, BigDecimal valor) implements MeioDePagamento {
    }
}
