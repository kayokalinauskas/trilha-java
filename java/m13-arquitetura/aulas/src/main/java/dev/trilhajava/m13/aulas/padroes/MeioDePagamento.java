package dev.trilhajava.m13.aulas.padroes;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Os meios de pagamento aceitos no checkout. A interface é selada: só os três records
 * abaixo podem implementá-la, e o compilador sabe disso quando você faz um switch.
 */
public sealed interface MeioDePagamento {

    record Pix(String chave) implements MeioDePagamento {
        public Pix {
            Objects.requireNonNull(chave, "chave");
        }
    }

    record Cartao(String bandeira, int parcelas) implements MeioDePagamento {
        public Cartao {
            Objects.requireNonNull(bandeira, "bandeira");
            if (parcelas < 1 || parcelas > 12) {
                throw new IllegalArgumentException("parcelas deve estar entre 1 e 12: " + parcelas);
            }
        }
    }

    record Boleto(LocalDate vencimento) implements MeioDePagamento {
        public Boleto {
            Objects.requireNonNull(vencimento, "vencimento");
        }
    }
}
