package dev.trilhajava.m13.aulas.padroes.fornecedor;

/** A resposta do fornecedor: status "APPROVED" ou "DECLINED". */
public record PagaFacilResposta(String status, String authCode, String declineReason) {
}
