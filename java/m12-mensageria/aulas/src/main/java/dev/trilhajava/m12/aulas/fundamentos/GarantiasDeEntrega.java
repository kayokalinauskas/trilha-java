package dev.trilhajava.m12.aulas.fundamentos;

/**
 * Passos 4 e 5: o mesmo consumidor de fidelidade, com o mesmo crash no mesmo ponto,
 * em três configurações. O que muda é só a ordem entre confirmar e processar, e a deduplicação.
 */
public class GarantiasDeEntrega {

    public static void main(String[] args) {
        rodar("no máximo uma vez     ", Garantia.NO_MAXIMO_UMA_VEZ, false);
        rodar("pelo menos uma vez    ", Garantia.PELO_MENOS_UMA_VEZ, false);
        rodar("pelo menos uma vez + idempotente", Garantia.PELO_MENOS_UMA_VEZ, true);
    }

    private static void rodar(String rotulo, Garantia garantia, boolean idempotente) {
        var log = new LogDeEventos<PedidoPago>();
        log.publicar(Exemplos.pedidoPago("PED-2001", "cli-7", "259.90"));
        log.publicar(Exemplos.pedidoPago("PED-2002", "cli-7", "100.00"));

        var fidelidade = new ProgramaDeFidelidade();
        var consumidor = new ConsumidorDoLog(log, "fidelidade", fidelidade, garantia, idempotente);

        consumidor.cairNoMeioDoProximo();
        try {
            consumidor.processarPendentes();
        } catch (QuedaSimuladaException e) {
            System.out.println("[" + rotulo.strip() + "] " + e.getMessage() + "; reiniciando...");
        }
        consumidor.processarPendentes(); // o processo "voltou" e lê a partir do último offset confirmado

        System.out.println(rotulo + " -> saldo de cli-7: " + fidelidade.saldo("cli-7")
                + " pontos (o correto é 359)");
    }
}
