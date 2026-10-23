package dev.trilhajava.fintech.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.Notificacao;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.RepositorioDeNotificacoes;
import dev.trilhajava.fintech.dominio.TipoConta;
import dev.trilhajava.fintech.dominio.TipoNotificacao;
import dev.trilhajava.fintech.dominio.TransferenciaRealizada;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Marco 5 — serviço de notificações (consumidor idempotente)")
class ServicoDeNotificacoesTest {

    static final Instant AGORA = Instant.parse("2026-09-29T15:00:00Z");
    static final UUID ANA = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000000");
    static final UUID BIA = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000000");
    static final UUID ORIGEM = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    static final UUID DESTINO = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @Mock
    RepositorioDeNotificacoes notificacoes;
    @Mock
    RepositorioDeContas contas;
    @Mock
    RegistroDeEventosProcessados processados;

    ServicoDeNotificacoes servico;

    final TransferenciaRealizada evento =
            new TransferenciaRealizada(UUID.randomUUID(), ORIGEM, DESTINO, new BigDecimal("150.00"), AGORA);

    @BeforeEach
    void criar() {
        servico = new ServicoDeNotificacoes(notificacoes, contas, processados, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    static Conta conta(UUID id, UUID dono) {
        return new Conta(id, "Titular", new Cpf("52998224725"), TipoConta.CORRENTE, AGORA, dono);
    }

    @Test
    @DisplayName("evento novo: avisa o dono da origem (enviada) e o do destino (recebida)")
    void novo() {
        UUID id = UUID.randomUUID();
        given(processados.registrar("notificacoes", id)).willReturn(true);
        given(contas.buscar(ORIGEM)).willReturn(Optional.of(conta(ORIGEM, ANA)));
        given(contas.buscar(DESTINO)).willReturn(Optional.of(conta(DESTINO, BIA)));

        assertThat(servico.registrar(id, evento)).isTrue();

        var salvas = ArgumentCaptor.forClass(Notificacao.class);
        then(notificacoes).should(times(2)).salvar(salvas.capture());
        List<Notificacao> lista = salvas.getAllValues();
        assertThat(lista).extracting(Notificacao::usuario, Notificacao::tipo).containsExactly(
                org.assertj.core.groups.Tuple.tuple(ANA, TipoNotificacao.TRANSFERENCIA_ENVIADA),
                org.assertj.core.groups.Tuple.tuple(BIA, TipoNotificacao.TRANSFERENCIA_RECEBIDA));
        assertThat(lista.getFirst().mensagem()).contains("150,00");
    }

    @Test
    @DisplayName("evento repetido: não consulta contas nem grava notificações")
    void repetido() {
        UUID id = UUID.randomUUID();
        given(processados.registrar("notificacoes", id)).willReturn(false);

        assertThat(servico.registrar(id, evento)).isFalse();

        then(notificacoes).should(times(0)).salvar(any());
        then(contas).shouldHaveNoInteractions();
    }
}
