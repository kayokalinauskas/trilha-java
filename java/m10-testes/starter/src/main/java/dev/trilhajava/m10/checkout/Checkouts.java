package dev.trilhajava.m10.checkout;

import java.time.Clock;

/**
 * Fábrica do checkout. Nos testes do exercício 10.02, basta {@code new ServicoDeCheckout(...)}.
 */
public final class Checkouts {

    @FunctionalInterface
    interface Fabrica {
        Checkout criar(Estoque e, GatewayDePagamento g, RepositorioDePedidos r, Notificador n, Clock c);
    }

    private static volatile Fabrica fabrica = ServicoDeCheckout::new;

    private Checkouts() {
    }

    public static Checkout novo(Estoque estoque, GatewayDePagamento gateway, RepositorioDePedidos repositorio,
            Notificador notificador, Clock clock) {
        return fabrica.criar(estoque, gateway, repositorio, notificador, clock);
    }

    static void usar(Fabrica outra) {
        fabrica = outra;
    }

    static void restaurar() {
        fabrica = ServicoDeCheckout::new;
    }
}
