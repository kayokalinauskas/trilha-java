package dev.trilhajava.m13.frete.modalidades;

import dev.trilhajava.m13.frete.Cotacao;
import dev.trilhajava.m13.frete.ModalidadeDeFrete;
import dev.trilhajava.m13.frete.Pedido;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** O PAC × 1,8, na metade do prazo (arredondada para cima). Composição: usa o PAC em vez de copiar a tabela. */
@Component
public class Sedex implements ModalidadeDeFrete {

    private static final BigDecimal FATOR = new BigDecimal("1.8");

    private final Pac pac;

    public Sedex(Pac pac) {
        this.pac = pac;
    }

    @Override
    public Optional<Cotacao> cotar(Pedido pedido) {
        return pac.cotar(pedido).map(base -> new Cotacao("SEDEX",
                base.valor().multiply(FATOR), (base.prazoEmDiasUteis() + 1) / 2));
    }
}
