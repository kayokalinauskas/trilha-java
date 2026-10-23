package dev.trilhajava.m12.aulas.fundamentos;

import java.time.Duration;

/** Passo 2: o checkout só publica PedidoPago; cada vizinho consome no seu ritmo. */
public class CheckoutComEventos {

    public static void main(String[] args) {
        var log = new LogDeEventos<PedidoPago>();
        var email = new ServicoSimulado("e-mail", Duration.ofMillis(350));
        var nota = new ServicoSimulado("nota-fiscal", Duration.ofMillis(180));
        var consumidorDeEmail = new ConsumidorDoLog(log, "email", email, Garantia.PELO_MENOS_UMA_VEZ, true);
        var consumidorDeNota = new ConsumidorDoLog(log, "nota-fiscal", nota, Garantia.PELO_MENOS_UMA_VEZ, true);

        email.derrubar();
        long offset = log.publicar(Exemplos.pedidoPago("PED-1002", "cli-8", "89.90"));
        System.out.println("PED-1002 confirmado ao cliente; evento gravado no offset " + offset);

        System.out.println("nota-fiscal processou " + consumidorDeNota.processarPendentes() + " evento(s)");
        try {
            consumidorDeEmail.processarPendentes();
        } catch (ServicoIndisponivelException e) {
            System.out.println("e-mail falhou: " + e.getMessage() + " (o evento continua no log)");
        }
        System.out.println("atraso do grupo email: " + log.atraso("email")
                + " | atraso do grupo nota-fiscal: " + log.atraso("nota-fiscal"));

        email.religar();
        System.out.println("e-mail voltou e processou " + consumidorDeEmail.processarPendentes() + " evento(s): "
                + email.atendidos());
        System.out.println("atraso do grupo email: " + log.atraso("email"));
    }
}
