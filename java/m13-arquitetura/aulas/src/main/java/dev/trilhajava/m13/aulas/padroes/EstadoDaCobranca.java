package dev.trilhajava.m13.aulas.padroes;

import java.time.Instant;

/**
 * State com tipos selados: cada estado é um record imutável com os dados que só fazem
 * sentido nele (o código de autorização só existe depois de autorizar). Cada transição
 * devolve um NOVO estado ou recusa, e o switch sem default obriga a pensar em todos.
 */
public sealed interface EstadoDaCobranca {

    record Pendente() implements EstadoDaCobranca {
    }

    record Autorizada(String codigo) implements EstadoDaCobranca {
    }

    record Capturada(String codigo, Instant em) implements EstadoDaCobranca {
    }

    record Estornada(String codigo, String motivo) implements EstadoDaCobranca {
    }

    record Recusada(String motivo) implements EstadoDaCobranca {
    }

    default EstadoDaCobranca autorizar(String codigo) {
        return switch (this) {
            case Pendente _ -> new Autorizada(codigo);
            case Autorizada _, Capturada _, Estornada _, Recusada _ -> throw invalida("autorizar");
        };
    }

    default EstadoDaCobranca recusar(String motivo) {
        return switch (this) {
            case Pendente _ -> new Recusada(motivo);
            case Autorizada _, Capturada _, Estornada _, Recusada _ -> throw invalida("recusar");
        };
    }

    default EstadoDaCobranca capturar(Instant agora) {
        return switch (this) {
            case Autorizada(String codigo) -> new Capturada(codigo, agora);
            case Pendente _, Capturada _, Estornada _, Recusada _ -> throw invalida("capturar");
        };
    }

    default EstadoDaCobranca estornar(String motivo) {
        return switch (this) {
            // Autorizada sem captura: cancela a reserva no cartão. Capturada: devolve o dinheiro.
            case Autorizada(String codigo) -> new Estornada(codigo, motivo);
            case Capturada(String codigo, _) -> new Estornada(codigo, motivo);
            case Pendente _, Estornada _, Recusada _ -> throw invalida("estornar");
        };
    }

    private TransicaoInvalidaException invalida(String acao) {
        return new TransicaoInvalidaException(
                "não é possível " + acao + " uma cobrança no estado " + getClass().getSimpleName());
    }

    class TransicaoInvalidaException extends IllegalStateException {
        public TransicaoInvalidaException(String mensagem) {
            super(mensagem);
        }
    }
}
