package dev.trilhajava.fintech.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.RepositorioDeMovimentos;
import dev.trilhajava.fintech.dominio.RepositorioDeTransferencias;
import dev.trilhajava.fintech.dominio.SaldoInsuficienteException;
import dev.trilhajava.fintech.dominio.TipoConta;
import dev.trilhajava.fintech.dominio.Transferencia;
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

    @Mock
    RepositorioDeContas contas;
    @Mock
    RepositorioDeMovimentos movimentos;
    @Mock
    RepositorioDeTransferencias transferencias;

    ServicoDeTransferencias servico;

    @BeforeEach
    void criar() {
        servico = new ServicoDeTransferencias(contas, movimentos, transferencias, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    static Conta conta(UUID id, String saldo) {
        var conta = new Conta(id, "Titular", new Cpf("52998224725"), TipoConta.CORRENTE, AGORA);
        if (new BigDecimal(saldo).signum() > 0) {
            conta.depositar(new BigDecimal(saldo), AGORA);
        }
        return conta;
    }

    @Test
    @DisplayName("mesma chave e mesmo pedido: devolve a transferência anterior sem travar nenhuma conta")
    void repeticao() {
        var anterior = new Transferencia(UUID.randomUUID(), MAIOR, MENOR, BigDecimal.TEN, AGORA, "chave-1");
        given(transferencias.buscarPorChave("chave-1")).willReturn(Optional.of(anterior));

        var resultado = servico.transferir("chave-1", MAIOR, MENOR, BigDecimal.TEN);

        assertThat(resultado.repetida()).isTrue();
        assertThat(resultado.transferencia()).isSameAs(anterior);
        then(contas).shouldHaveNoInteractions();
        then(movimentos).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("trava as contas sempre da menor para a maior, mesmo quando a origem é a maior (evita deadlock)")
    void ordemDosLocks() {
        given(transferencias.buscarPorChave("chave-2")).willReturn(Optional.empty());
        given(contas.buscarParaAtualizar(MENOR)).willReturn(Optional.of(conta(MENOR, "0.00")));
        given(contas.buscarParaAtualizar(MAIOR)).willReturn(Optional.of(conta(MAIOR, "100.00")));

        servico.transferir("chave-2", MAIOR, MENOR, new BigDecimal("40.00"));

        InOrder ordem = inOrder(contas);
        ordem.verify(contas).buscarParaAtualizar(MENOR);
        ordem.verify(contas).buscarParaAtualizar(MAIOR);
        then(transferencias).should().salvar(any());
        then(movimentos).should(org.mockito.Mockito.times(2)).registrar(any());
    }

    @Test
    @DisplayName("saldo insuficiente: nenhuma transferência nem movimento é gravado")
    void saldoInsuficiente() {
        given(transferencias.buscarPorChave("chave-3")).willReturn(Optional.empty());
        given(contas.buscarParaAtualizar(MENOR)).willReturn(Optional.of(conta(MENOR, "5.00")));
        given(contas.buscarParaAtualizar(MAIOR)).willReturn(Optional.of(conta(MAIOR, "5.00")));

        assertThatThrownBy(() -> servico.transferir("chave-3", MENOR, MAIOR, new BigDecimal("10.00")))
                .isInstanceOf(SaldoInsuficienteException.class);
        then(movimentos).should(never()).registrar(any());
    }
}
