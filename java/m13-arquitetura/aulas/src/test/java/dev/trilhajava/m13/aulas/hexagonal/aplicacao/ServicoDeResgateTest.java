package dev.trilhajava.m13.aulas.hexagonal.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ComandoDeResgate;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CarteirasDePontos;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CatalogoDeRecompensas;
import dev.trilhajava.m13.aulas.hexagonal.dominio.CarteiraDePontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Pontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Recompensa;
import dev.trilhajava.m13.aulas.hexagonal.dominio.RecompensaResgatada;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Resgate;
import dev.trilhajava.m13.aulas.hexagonal.dominio.SaldoInsuficienteException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** O núcleo testado com new e fakes: sem Spring, sem banco, sem HTTP. */
class ServicoDeResgateTest {

    static final Instant AGORA = Instant.parse("2026-10-06T13:00:00Z");

    /** Fake da porta de saída: um Map faz as vezes da tabela. */
    static class CarteirasEmMemoria implements CarteirasDePontos {
        final Map<String, CarteiraDePontos> porCliente = new HashMap<>();

        @Override
        public Optional<CarteiraDePontos> doCliente(String clienteId) {
            return Optional.ofNullable(porCliente.get(clienteId));
        }

        @Override
        public void salvar(CarteiraDePontos carteira) {
            porCliente.put(carteira.clienteId(), carteira);
        }
    }

    final CarteirasEmMemoria carteiras = new CarteirasEmMemoria();
    final CatalogoDeRecompensas catalogo = codigo -> codigo.equals("CAFE-GRATIS")      // uma lambda basta
            ? Optional.of(new Recompensa("CAFE-GRATIS", "Um café", Pontos.de(500), true))
            : Optional.empty();
    final List<RecompensaResgatada> publicados = new ArrayList<>();
    final ServicoDeResgate servico = new ServicoDeResgate(
            carteiras, catalogo, publicados::add, Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void debitaOsPontosGravaEPublicaOEvento() {
        carteiras.salvar(CarteiraDePontos.reconstituir("cli-ana", Pontos.de(1200), 0));

        Resgate resgate = servico.resgatar(new ComandoDeResgate("cli-ana", "CAFE-GRATIS"));

        assertThat(resgate.saldoRestante()).isEqualTo(Pontos.de(700));
        assertThat(resgate.realizadoEm()).isEqualTo(AGORA);
        assertThat(carteiras.doCliente("cli-ana").orElseThrow().saldo()).isEqualTo(Pontos.de(700));
        assertThat(publicados).containsExactly(
                new RecompensaResgatada(resgate.id(), "cli-ana", "CAFE-GRATIS", 500, AGORA));
    }

    @Test
    void saldoInsuficienteNaoGravaNemPublica() {
        carteiras.salvar(CarteiraDePontos.reconstituir("cli-bia", Pontos.de(300), 0));

        assertThatThrownBy(() -> servico.resgatar(new ComandoDeResgate("cli-bia", "CAFE-GRATIS")))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("cliente cli-bia tem 300 pontos e a recompensa custa 500");

        assertThat(carteiras.doCliente("cli-bia").orElseThrow().saldo()).isEqualTo(Pontos.de(300));
        assertThat(publicados).isEmpty();
    }
}
