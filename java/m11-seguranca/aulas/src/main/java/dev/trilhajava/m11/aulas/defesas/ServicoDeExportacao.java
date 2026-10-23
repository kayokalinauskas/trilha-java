package dev.trilhajava.m11.aulas.defesas;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Pedir a exportação de um extrato é caro (gera um arquivo grande) e não é idempotente: repetir o POST
 * criaria outra exportação. Com a Idempotency-Key, a repetição devolve a MESMA exportação.
 *
 * <p>Em memória para a aula. Em produção: uma tabela com UNIQUE (usuario, chave), o hash do corpo,
 * a resposta gravada e uma validade (ex.: 24 h), na mesma transação que cria a exportação.
 */
@Service
public class ServicoDeExportacao {

    public record Resultado(Exportacao exportacao, boolean repetida) {
    }

    private record Registro(PedidoDeExportacao pedido, Exportacao exportacao) {
    }

    private final Map<String, Registro> porChave = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    public Resultado exportar(String usuario, String chave, PedidoDeExportacao pedido) {
        String escopo = usuario + ":" + chave;          // a chave vale só para quem a criou
        boolean[] criada = {false};
        Registro registro = porChave.computeIfAbsent(escopo, k -> {
            criada[0] = true;
            var nova = new Exportacao("EXP-" + sequencia.incrementAndGet(), pedido.formato(), "EM_PROCESSAMENTO");
            return new Registro(pedido, nova);
        });
        if (!registro.pedido().equals(pedido)) {
            throw new ChaveDeIdempotenciaReutilizadaException(chave);
        }
        return new Resultado(registro.exportacao(), !criada[0]);
    }
}
