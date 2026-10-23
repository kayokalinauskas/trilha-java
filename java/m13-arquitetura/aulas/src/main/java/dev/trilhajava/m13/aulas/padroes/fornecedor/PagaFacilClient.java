package dev.trilhajava.m13.aulas.padroes.fornecedor;

/**
 * Faz de conta que este pacote é o SDK de um fornecedor de pagamentos, a "PagaFácil".
 * Repare no estilo dele: valores em centavos, método como texto em inglês, status como
 * String e falhas como exceção própria. Nada disso deve vazar para o domínio.
 */
public interface PagaFacilClient {

    /**
     * @param amountInCents   valor em centavos
     * @param method          "PIX", "CARD" ou "BOLETO"
     * @param idempotencyKey  a mesma chave devolve a mesma transação, sem cobrar de novo
     * @throws PagaFacilIndisponivelException quando o serviço está fora do ar
     */
    PagaFacilResposta createTransaction(long amountInCents, String method, String idempotencyKey);
}
