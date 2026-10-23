package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Boleto;
import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Cartao;
import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Pix;
import dev.trilhajava.m13.aulas.padroes.fornecedor.PagaFacilClient;
import dev.trilhajava.m13.aulas.padroes.fornecedor.PagaFacilIndisponivelException;
import dev.trilhajava.m13.aulas.padroes.fornecedor.PagaFacilResposta;

/**
 * Adapter: traduz a interface do domínio ({@link GatewayDePagamento}) para o SDK da
 * PagaFácil e a resposta dele de volta. É o único arquivo do sistema que conhece
 * centavos, "CARD" e "DECLINED".
 */
public class AdaptadorPagaFacil implements GatewayDePagamento {

    private final PagaFacilClient cliente;

    public AdaptadorPagaFacil(PagaFacilClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public ResultadoDaCobranca cobrar(SolicitacaoDeCobranca solicitacao) {
        long centavos = solicitacao.valor().movePointRight(2).longValueExact();
        PagaFacilResposta resposta;
        try {
            resposta = cliente.createTransaction(centavos, metodo(solicitacao.meio()),
                    solicitacao.chaveDeIdempotencia());
        } catch (PagaFacilIndisponivelException e) {
            throw new FalhaTemporariaException("PagaFácil indisponível (HTTP " + e.httpStatus() + ")", e);
        }
        return switch (resposta.status()) {
            case "APPROVED" -> new ResultadoDaCobranca.Aprovada(resposta.authCode());
            case "DECLINED" -> new ResultadoDaCobranca.Recusada(resposta.declineReason());
            default -> throw new IllegalStateException("status desconhecido da PagaFácil: " + resposta.status());
        };
    }

    private static String metodo(MeioDePagamento meio) {
        return switch (meio) {
            case Pix _ -> "PIX";
            case Cartao _ -> "CARD";
            case Boleto _ -> "BOLETO";
        };
    }
}
