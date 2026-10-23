package dev.trilhajava.m09.imoveis;

import java.math.BigDecimal;

/** O card do imóvel na listagem. */
public record ImovelResumo(long id, String titulo, String cidade, String bairro, BigDecimal preco, int quartos) {
}
