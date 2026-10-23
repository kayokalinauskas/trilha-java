package dev.trilhajava.m13.frete.modalidades;

import dev.trilhajava.m13.frete.Cotacao;
import dev.trilhajava.m13.frete.ModalidadeDeFrete;
import dev.trilhajava.m13.frete.Pedido;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Grátis, em 1 dia útil, só na capital paulista (CEP 01000-000 a 05999-999). */
@Component
public class RetiradaNaLoja implements ModalidadeDeFrete {

    @Override
    public Optional<Cotacao> cotar(Pedido pedido) {
        int prefixo = Integer.parseInt(pedido.cep().substring(0, 2));
        if (prefixo < 1 || prefixo > 5) {
            return Optional.empty();
        }
        return Optional.of(new Cotacao("RETIRADA", BigDecimal.ZERO, 1));
    }
}
