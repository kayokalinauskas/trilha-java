package dev.trilhajava.m13.aulas.resiliencia;

import java.time.Duration;

/**
 * O circuito abrindo, recusando sem tocar na operadora e fechando de novo quando ela volta.
 * Janela de 4 chamadas, abre com 50% de falhas, fica aberto 1 s.
 *
 * <pre>mvn -q compile exec:java -Dexec.mainClass=dev.trilhajava.m13.aulas.resiliencia.DemoDoCircuito</pre>
 */
public final class DemoDoCircuito {

    public static void main(String[] args) throws InterruptedException {
        Logs.soAvisosDasBibliotecas();
        var politica = new PoliticaDeResiliencia(
                Duration.ofMillis(200), Duration.ofMillis(300),
                2, Duration.ofMillis(20), Duration.ofMillis(10), Duration.ofSeconds(2),
                4, 4, 50f, Duration.ofSeconds(1));

        try (var operadora = OperadoraFalsa.iniciar()) {
            var verificador = new VerificadorDeElegibilidade(
                    new ClienteDaOperadora(operadora.url(), politica.timeoutDeConexao(), politica.timeoutDeLeitura()),
                    politica);

            operadora.fora(true);
            System.out.println("-- operadora fora do ar");
            for (int i = 1; i <= 6; i++) {
                consultar(verificador, operadora, i);
            }

            Thread.sleep(1_100);
            operadora.fora(false);
            System.out.println("-- 1,1 s depois, operadora de volta");
            for (int i = 7; i <= 9; i++) {
                consultar(verificador, operadora, i);
            }
        }
    }

    private static void consultar(VerificadorDeElegibilidade verificador, OperadoraFalsa operadora, int n) {
        Elegibilidade resultado = verificador.verificar("89001234");
        System.out.printf("consulta %d: %-14s | operadora recebeu %2d requisições | circuito %s%n",
                n, resultado.situacao(), operadora.chamadasRecebidas(), verificador.estadoDoCircuito());
    }
}
