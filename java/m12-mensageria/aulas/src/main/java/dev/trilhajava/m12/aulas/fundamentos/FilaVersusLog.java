package dev.trilhajava.m12.aulas.fundamentos;

/** Passo 3: a mesma sequência de mensagens numa fila de trabalho e num log de eventos. */
public class FilaVersusLog {

    public static void main(String[] args) {
        var fila = new FilaDeTrabalho<String>();
        for (String pedido : new String[] {"PED-1", "PED-2", "PED-3"}) {
            fila.enviar("separar " + pedido);
        }
        var paraAna = fila.receber().orElseThrow();
        var paraBruno = fila.receber().orElseThrow();
        System.out.println("worker-ana recebeu  : " + paraAna.mensagem());
        System.out.println("worker-bruno recebeu: " + paraBruno.mensagem());
        fila.confirmar(paraAna.etiqueta());
        System.out.println("ana confirmou; prontas=" + fila.prontas() + ", sem ack=" + fila.semAck());

        fila.consumidorCaiu(); // a conexão do bruno caiu antes do ack
        var reentrega = fila.receber().orElseThrow();
        System.out.println("depois da queda: " + reentrega.mensagem() + " (reentregue=" + reentrega.reentregue() + ")");

        var log = new LogDeEventos<String>();
        for (String pedido : new String[] {"PED-1", "PED-2", "PED-3"}) {
            log.publicar("PedidoPago " + pedido);
        }
        for (var registro : log.ler("faturamento", 10)) {
            System.out.println("faturamento leu offset " + registro.offset() + ": " + registro.valor());
        }
        log.confirmar("faturamento", 3);
        System.out.println("faturamento confirmou até 3; o log ainda tem " + log.tamanho() + " registros");
        System.out.println("grupo novo 'dados' começa do início e lê " + log.ler("dados", 10).size() + " registros");
    }
}
