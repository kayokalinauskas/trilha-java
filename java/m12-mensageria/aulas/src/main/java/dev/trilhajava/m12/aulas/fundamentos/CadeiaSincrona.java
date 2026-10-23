package dev.trilhajava.m12.aulas.fundamentos;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/** Passo 1: o checkout que chama quatro vizinhos por HTTP, um depois do outro. */
public class CadeiaSincrona {

    public static void main(String[] args) {
        var nota = new ServicoSimulado("nota-fiscal", Duration.ofMillis(180));
        var estoque = new ServicoSimulado("estoque", Duration.ofMillis(40));
        var email = new ServicoSimulado("e-mail", Duration.ofMillis(350));
        var pontos = new ServicoSimulado("fidelidade", Duration.ofMillis(60));
        var checkout = new CheckoutSincrono(List.of(nota, estoque, email, pontos));

        Duration espera = checkout.pagar(Exemplos.pedidoPago("PED-1001", "cli-7", "259.90"));
        System.out.println("PED-1001 confirmado; o cliente esperou " + espera.toMillis() + " ms");

        email.derrubar();
        try {
            checkout.pagar(Exemplos.pedidoPago("PED-1002", "cli-8", "89.90"));
        } catch (ServicoIndisponivelException e) {
            System.out.println("PED-1002 falhou no checkout: " + e.getMessage());
        }
        System.out.println("nota-fiscal já atendeu: " + nota.atendidos());
        System.out.println("estoque já atendeu:     " + estoque.atendidos());

        double cadeia = CheckoutSincrono.disponibilidadeDaCadeia(0.999, 0.999, 0.999, 0.999, 0.999);
        double minutosForaPorMes = (1 - cadeia) * 30 * 24 * 60;
        System.out.printf(Locale.ROOT, "disponibilidade de 5 elos a 99,9%%: %.4f%% (%.0f min fora do ar por mês)%n",
                cadeia * 100, minutosForaPorMes);
    }
}
