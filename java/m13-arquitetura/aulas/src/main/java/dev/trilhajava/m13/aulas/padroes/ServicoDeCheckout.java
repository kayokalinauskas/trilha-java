package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.ResultadoDaCobranca.Aprovada;
import dev.trilhajava.m13.aulas.padroes.ResultadoDaCobranca.Recusada;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

/**
 * Junta os padrões: a cadeia de antifraude decide se cobra, o gateway (já decorado) cobra,
 * e um evento avisa quem se interessar. O checkout não conhece a nota fiscal nem a
 * fidelidade: só publica que o pagamento foi aprovado.
 */
public class ServicoDeCheckout {

    private final AnaliseAntifraude antifraude;
    private final GatewayDePagamento gateway;
    private final ApplicationEventPublisher eventos;

    public ServicoDeCheckout(AnaliseAntifraude antifraude, GatewayDePagamento gateway,
                             ApplicationEventPublisher eventos) {
        this.antifraude = antifraude;
        this.gateway = gateway;
        this.eventos = eventos;
    }

    public ResultadoDaCobranca pagar(SolicitacaoDeCobranca solicitacao) {
        Optional<String> recusa = antifraude.avaliar(solicitacao);
        if (recusa.isPresent()) {
            return new Recusada("antifraude: " + recusa.get());
        }
        ResultadoDaCobranca resultado = gateway.cobrar(solicitacao);
        if (resultado instanceof Aprovada(String codigo)) {
            eventos.publishEvent(new PagamentoAprovado(
                    solicitacao.pedidoId(), solicitacao.clienteId(), solicitacao.valor(), codigo));
        }
        return resultado;
    }
}
