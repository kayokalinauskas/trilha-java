package dev.trilhajava.m13.frete.modalidades;

import dev.trilhajava.m13.frete.Regiao;
import java.math.BigDecimal;

/** O preço base e o prazo do PAC por região: dados, e não uma cadeia de ternários. */
record TabelaPorRegiao(BigDecimal base, int prazo) {

    static TabelaPorRegiao de(Regiao regiao) {
        return switch (regiao) {
            case SUDESTE -> new TabelaPorRegiao(new BigDecimal("18.90"), 5);
            case SUL -> new TabelaPorRegiao(new BigDecimal("22.90"), 6);
            case CENTRO_OESTE -> new TabelaPorRegiao(new BigDecimal("26.90"), 8);
            case NORTE_NORDESTE -> new TabelaPorRegiao(new BigDecimal("32.90"), 10);
        };
    }
}
