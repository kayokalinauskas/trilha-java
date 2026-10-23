package dev.trilhajava.m13.loja.estoque;

import dev.trilhajava.m13.loja.estoque.internal.Saldos;
import org.springframework.stereotype.Service;

/** A API pública do módulo estoque: consultas. A baixa acontece por evento, não por chamada. */
@Service
public class Estoque {

    private final Saldos saldos;

    Estoque(Saldos saldos) {
        this.saldos = saldos;
    }

    public int disponivel(String sku) {
        return saldos.de(sku);
    }
}
