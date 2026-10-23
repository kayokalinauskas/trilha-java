package dev.trilhajava.m04.eventos;

import static java.util.stream.Collectors.groupingBy;

import dev.trilhajava.m04.eventos.EventoConta.ContaAberta;
import dev.trilhajava.m04.eventos.EventoConta.ContaBloqueada;
import dev.trilhajava.m04.eventos.EventoConta.Deposito;
import dev.trilhajava.m04.eventos.EventoConta.Saque;
import dev.trilhajava.m04.eventos.EventoConta.TransferenciaRecebida;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Solução de referência do exercício 04.04. */
public final class ProjecaoConta {

    private ProjecaoConta() {
    }

    public static EstadoConta reconstruir(List<EventoConta> eventos) {
        if (eventos.isEmpty()) {
            throw new IllegalStateException("Histórico vazio: é preciso um evento ContaAberta");
        }
        long contas = eventos.stream().map(EventoConta::contaId).distinct().count();
        if (contas > 1) {
            throw new IllegalArgumentException("Eventos de " + contas + " contas diferentes; use reconstruirTodas");
        }
        List<EventoConta> ordenados = eventos.stream().sorted(Comparator.comparing(EventoConta::momento)).toList();
        if (!(ordenados.getFirst() instanceof ContaAberta)) {
            throw new IllegalStateException("O primeiro evento da conta precisa ser ContaAberta, veio " + ordenados.getFirst());
        }
        EstadoConta estado = null;
        for (EventoConta evento : ordenados) {
            estado = aplicar(estado, evento);
        }
        return estado;
    }

    /** Uma função pura (estado, evento) -> novo estado: o coração do event sourcing. */
    private static EstadoConta aplicar(EstadoConta e, EventoConta evento) {
        return switch (evento) {
            case ContaAberta(String conta, var momento, String titular) ->
                    new EstadoConta(conta, titular, BigDecimal.ZERO, false, 1);
            case Deposito d -> new EstadoConta(e.contaId(), e.titular(), e.saldo().add(d.valor()), e.bloqueada(), e.eventosAplicados() + 1);
            case Saque s -> new EstadoConta(e.contaId(), e.titular(), e.saldo().subtract(s.valor()), e.bloqueada(), e.eventosAplicados() + 1);
            case TransferenciaRecebida t ->
                    new EstadoConta(e.contaId(), e.titular(), e.saldo().add(t.valor()), e.bloqueada(), e.eventosAplicados() + 1);
            case ContaBloqueada b -> new EstadoConta(e.contaId(), e.titular(), e.saldo(), true, e.eventosAplicados() + 1);
        };
    }

    public static Map<String, EstadoConta> reconstruirTodas(List<EventoConta> eventos) {
        return eventos.stream()
                .collect(groupingBy(EventoConta::contaId, TreeMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), ProjecaoConta::reconstruir)));
    }

    public static String descrever(EventoConta evento) {
        return switch (evento) {
            case ContaAberta(var conta, var momento, String titular) -> "Conta aberta para " + titular;
            case Deposito(var conta, var momento, BigDecimal valor) -> "Depósito de R$ " + valor;
            case Saque(var conta, var momento, BigDecimal valor) -> "Saque de R$ " + valor;
            case TransferenciaRecebida(var conta, var momento, BigDecimal valor, String origem) ->
                    "Transferência de R$ " + valor + " recebida de " + origem;
            case ContaBloqueada(var conta, var momento, String motivo) -> "Conta bloqueada: " + motivo;
        };
    }

    public static BigDecimal totalMovimentado(List<EventoConta> eventos, LocalDate dia, ZoneId fuso) {
        return eventos.stream()
                .filter(e -> e.momento().atZone(fuso).toLocalDate().equals(dia))
                .map(ProjecaoConta::valorMovimentado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal valorMovimentado(EventoConta evento) {
        return switch (evento) {
            case Deposito d -> d.valor();
            case Saque s -> s.valor();
            case TransferenciaRecebida t -> t.valor();
            case ContaAberta a -> BigDecimal.ZERO;
            case ContaBloqueada b -> BigDecimal.ZERO;
        };
    }
}
