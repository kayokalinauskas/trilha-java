package dev.trilhajava.m13.frete;

/** Ajusta uma cotação já calculada (seguro, frete grátis...). Devolve a mesma cotação se não se aplica. */
public interface RegraDePromocao {

    Cotacao aplicar(Pedido pedido, Cotacao cotacao);
}
