package dev.trilhajava.fintech.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.ContaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.RepositorioDeMovimentos;
import dev.trilhajava.fintech.dominio.RepositorioDeTransferencias;
import dev.trilhajava.fintech.dominio.SaldoInsuficienteException;
import dev.trilhajava.fintech.dominio.TipoConta;
import dev.trilhajava.fintech.dominio.Transferencia;
import dev.trilhajava.fintech.dominio.TransferenciaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.TransferenciaRealizada;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** O caso de uso isolado das portas de persistência (mocks). Rápido e focado nas decisões do serviço. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Serviço de transferências (caso de uso)")
class ServicoDeTransferenciasTest {

    static final Instant AGORA = Instant.parse("2026-09-28T13:00:00Z");
    static final UUID MENOR = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID MAIOR = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID ANA = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000000");
    static final UUID BRUNO = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000000");
    static final Solicitante COMO_ANA = Solicitante.cliente(ANA);

    @Mock
    RepositorioDeContas contas;
    @Mock
    RepositorioDeMovimentos movimentos;
    @Mock
    RepositorioDeTransferencias transferencias;
    @Mock
    PublicadorDeEventos eventos;

    ServicoDeTransferencias servico;

    @BeforeEach
    void criar() {
        servico = new ServicoDeTransferencias(contas, movimentos, transferencias, eventos, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    static Conta conta(UUID id, UUID dono, String saldo) {
        var conta = new Conta(id, "Titular", new Cpf("52998224725"), TipoConta.CORRENTE, AGORA, dono);
        if (new BigDecimal(saldo).signum() > 0) {
            conta.depositar(new BigDecimal(saldo), AGORA);
        }
        return conta;
    }

    /** A origem é da Ana (a verificação de posse acontece antes de qualquer lock). */
    void origemDaAna(UUID origem) {
        given(contas.pertenceA(origem, ANA)).willReturn(true);
    }

    @Test
    @DisplayName("mesma chave e mesmo pedido: devolve a transferência anterior sem travar nenhuma conta")
    void repeticao() {
        origemDaAna(MAIOR);
        var anterior = new Transferencia(UUID.randomUUID(), MAIOR, MENOR, BigDecimal.TEN, AGORA, "chave-1");
        given(transferencias.buscarPorChave("chave-1")).willReturn(Optional.of(anterior));

        var resultado = servico.transferir(COMO_ANA, "chave-1", MAIOR, MENOR, BigDecimal.TEN);

        assertThat(resultado.repetida()).isTrue();
        assertThat(resultado.transferencia()).isSameAs(anterior);
        then(contas).should(never()).buscarParaAtualizar(any());
        then(movimentos).shouldHaveNoInteractions();
        then(eventos).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("trava as contas sempre da menor para a maior, mesmo quando a origem é a maior (evita deadlock)")
    void ordemDosLocks() {
        origemDaAna(MAIOR);
        given(transferencias.buscarPorChave("chave-2")).willReturn(Optional.empty());
        given(contas.buscarParaAtualizar(MENOR)).willReturn(Optional.of(conta(MENOR, BRUNO, "0.00")));
        given(contas.buscarParaAtualizar(MAIOR)).willReturn(Optional.of(conta(MAIOR, ANA, "100.00")));

        servico.transferir(COMO_ANA, "chave-2", MAIOR, MENOR, new BigDecimal("40.00"));

        InOrder ordem = inOrder(contas);
        ordem.verify(contas).buscarParaAtualizar(MENOR);
        ordem.verify(contas).buscarParaAtualizar(MAIOR);
        then(transferencias).should().salvar(any());
        then(movimentos).should(org.mockito.Mockito.times(2)).registrar(any());
    }

    @Test
    @DisplayName("marco 5: publica TransferenciaRealizada (na transação do caso de uso), com a origem como agregado")
    void publicaEvento() {
        origemDaAna(MAIOR);
        given(transferencias.buscarPorChave("chave-7")).willReturn(Optional.empty());
        given(contas.buscarParaAtualizar(MENOR)).willReturn(Optional.of(conta(MENOR, BRUNO, "0.00")));
        given(contas.buscarParaAtualizar(MAIOR)).willReturn(Optional.of(conta(MAIOR, ANA, "100.00")));

        var resultado = servico.transferir(COMO_ANA, "chave-7", MAIOR, MENOR, new BigDecimal("40.00"));

        then(eventos).should().publicar(new TransferenciaRealizada(resultado.transferencia().id(), MAIOR, MENOR,
                new BigDecimal("40.00"), AGORA));
    }

    @Test
    @DisplayName("saldo insuficiente: nenhuma transferência nem movimento é gravado")
    void saldoInsuficiente() {
        origemDaAna(MENOR);
        given(transferencias.buscarPorChave("chave-3")).willReturn(Optional.empty());
        given(contas.buscarParaAtualizar(MENOR)).willReturn(Optional.of(conta(MENOR, ANA, "5.00")));
        given(contas.buscarParaAtualizar(MAIOR)).willReturn(Optional.of(conta(MAIOR, BRUNO, "5.00")));

        assertThatThrownBy(() -> servico.transferir(COMO_ANA, "chave-3", MENOR, MAIOR, new BigDecimal("10.00")))
                .isInstanceOf(SaldoInsuficienteException.class);
        then(movimentos).should(never()).registrar(any());
        then(eventos).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("marco 4: origem de outra pessoa → conta não encontrada, antes de olhar a chave ou travar contas")
    void origemDeOutro() {
        given(contas.pertenceA(MENOR, ANA)).willReturn(false);

        assertThatThrownBy(() -> servico.transferir(COMO_ANA, "chave-4", MENOR, MAIOR, BigDecimal.ONE))
                .isInstanceOf(ContaNaoEncontradaException.class);
        then(transferencias).shouldHaveNoInteractions();
        then(contas).should(never()).buscarParaAtualizar(any());
    }

    @Test
    @DisplayName("marco 4: o ADMIN pode movimentar a partir de qualquer conta")
    void admin() {
        given(transferencias.buscarPorChave("chave-5")).willReturn(Optional.empty());
        given(contas.buscarParaAtualizar(MENOR)).willReturn(Optional.of(conta(MENOR, BRUNO, "50.00")));
        given(contas.buscarParaAtualizar(MAIOR)).willReturn(Optional.of(conta(MAIOR, ANA, "0.00")));

        var resultado = servico.transferir(Solicitante.administrador(UUID.randomUUID()), "chave-5", MENOR, MAIOR,
                new BigDecimal("50.00"));
        assertThat(resultado.repetida()).isFalse();
        then(contas).should(never()).pertenceA(any(), any());
    }

    @Test
    @DisplayName("marco 4: a transferência é visível para quem envia e quem recebe; para terceiros, não existe")
    void visibilidade() {
        var t = new Transferencia(UUID.randomUUID(), MENOR, MAIOR, BigDecimal.TEN, AGORA, "chave-6");
        given(transferencias.buscar(t.id())).willReturn(Optional.of(t));
        given(contas.buscar(MENOR)).willReturn(Optional.of(conta(MENOR, ANA, "0.00")));
        given(contas.buscar(MAIOR)).willReturn(Optional.of(conta(MAIOR, BRUNO, "0.00")));

        assertThat(servico.buscar(COMO_ANA, t.id())).isSameAs(t);
        assertThat(servico.buscar(Solicitante.cliente(BRUNO), t.id())).isSameAs(t);
        assertThatThrownBy(() -> servico.buscar(Solicitante.cliente(UUID.randomUUID()), t.id()))
                .isInstanceOf(TransferenciaNaoEncontradaException.class);
    }
}
