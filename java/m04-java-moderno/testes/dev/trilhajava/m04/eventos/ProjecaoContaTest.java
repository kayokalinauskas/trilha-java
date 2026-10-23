package dev.trilhajava.m04.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m04.eventos.EventoConta.ContaAberta;
import dev.trilhajava.m04.eventos.EventoConta.ContaBloqueada;
import dev.trilhajava.m04.eventos.EventoConta.Deposito;
import dev.trilhajava.m04.eventos.EventoConta.Saque;
import dev.trilhajava.m04.eventos.EventoConta.TransferenciaRecebida;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 04.04 — Projeção de conta a partir de eventos")
class ProjecaoContaTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");

    private static Instant t(String dataHora) {
        return LocalDate.parse(dataHora.substring(0, 10)).atTime(java.time.LocalTime.parse(dataHora.substring(11))).atZone(SP).toInstant();
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private final List<EventoConta> historico = List.of(
            new ContaAberta("A", t("2026-03-01T09:00"), "Ana"),
            new Deposito("A", t("2026-03-01T10:00"), bd("500.00")),
            new ContaAberta("B", t("2026-03-01T11:00"), "Bruno"),
            new Saque("A", t("2026-03-02T08:30"), bd("120.00")),
            new TransferenciaRecebida("B", t("2026-03-02T09:00"), bd("75.50"), "A"),
            new Deposito("B", t("2026-03-02T23:59"), bd("10.00")),
            new ContaBloqueada("A", t("2026-03-03T12:00"), "suspeita de fraude"));

    @Test
    @DisplayName("reconstrói o estado aplicando os eventos de uma conta em ordem")
    void reconstruir() {
        List<EventoConta> daConta = historico.stream().filter(e -> e.contaId().equals("A")).toList();
        EstadoConta a = ProjecaoConta.reconstruir(daConta);
        assertThat(a).isEqualTo(new EstadoConta("A", "Ana", bd("380.00"), true, 4));
    }

    @Test
    @DisplayName("a ordem de chegada não importa: os eventos são ordenados pelo momento")
    void foraDeOrdem() {
        List<EventoConta> embaralhado = List.of(historico.get(3), historico.get(1), historico.get(0));
        assertThat(ProjecaoConta.reconstruir(embaralhado).saldo()).isEqualByComparingTo("380.00");
    }

    @Test
    @DisplayName("histórico que não começa com ContaAberta é inválido")
    void semAbertura() {
        assertThatThrownBy(() -> ProjecaoConta.reconstruir(List.of(historico.get(1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ContaAberta");
        assertThatThrownBy(() -> ProjecaoConta.reconstruir(List.of())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("eventos de contas diferentes na mesma lista são rejeitados")
    void contasMisturadas() {
        assertThatThrownBy(() -> ProjecaoConta.reconstruir(historico)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("reconstrói todas as contas de um histórico misturado")
    void todas() {
        Map<String, EstadoConta> contas = ProjecaoConta.reconstruirTodas(historico);
        assertThat(contas.keySet()).containsExactly("A", "B");
        assertThat(contas.get("B")).isEqualTo(new EstadoConta("B", "Bruno", bd("85.50"), false, 3));
    }

    @Test
    @DisplayName("descreve cada evento para o extrato")
    void descrever() {
        assertThat(ProjecaoConta.descrever(historico.get(0))).isEqualTo("Conta aberta para Ana");
        assertThat(ProjecaoConta.descrever(historico.get(1))).isEqualTo("Depósito de R$ 500.00");
        assertThat(ProjecaoConta.descrever(historico.get(3))).isEqualTo("Saque de R$ 120.00");
        assertThat(ProjecaoConta.descrever(historico.get(4))).isEqualTo("Transferência de R$ 75.50 recebida de A");
        assertThat(ProjecaoConta.descrever(historico.get(6))).isEqualTo("Conta bloqueada: suspeita de fraude");
    }

    @Test
    @DisplayName("total movimentado por dia, no fuso de São Paulo")
    void movimentadoNoDia() {
        assertThat(ProjecaoConta.totalMovimentado(historico, LocalDate.of(2026, 3, 2), SP)).isEqualByComparingTo("205.50");
        assertThat(ProjecaoConta.totalMovimentado(historico, LocalDate.of(2026, 3, 5), SP)).isEqualByComparingTo("0");
    }
}
