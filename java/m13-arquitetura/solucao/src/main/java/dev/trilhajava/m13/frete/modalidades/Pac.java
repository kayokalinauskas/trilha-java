package dev.trilhajava.m13.frete.modalidades;

import dev.trilhajava.m13.frete.Cotacao;
import dev.trilhajava.m13.frete.ModalidadeDeFrete;
import dev.trilhajava.m13.frete.Pedido;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Até 30 kg. Preço base por região + R$ 2,50 por kg (ou fração) acima do 1º quilo. */
@Component
public class Pac implements ModalidadeDeFrete {

    static final int PESO_MAXIMO_EM_GRAMAS = 30_000;
    private static final BigDecimal POR_KG_EXCEDENTE = new BigDecimal("2.50");

    @Override
    public Optional<Cotacao> cotar(Pedido pedido) {
        if (pedido.pesoEmGramas() > PESO_MAXIMO_EM_GRAMAS) {
            return Optional.empty();
        }
        var tabela = TabelaPorRegiao.de(pedido.regiao());
        BigDecimal valor = tabela.base().add(POR_KG_EXCEDENTE.multiply(BigDecimal.valueOf(kgExcedentes(pedido))));
        return Optional.of(new Cotacao("PAC", valor, tabela.prazo()));
    }

    /** Quilos (ou fração) acima do primeiro: 1.000 g → 0; 1.001 g → 1; 3.200 g → 3. */
    static int kgExcedentes(Pedido pedido) {
        int excedente = Math.max(0, pedido.pesoEmGramas() - 1_000);
        return (excedente + 999) / 1_000;
    }
}
