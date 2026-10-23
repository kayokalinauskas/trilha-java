package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.EstadoDaCobranca.Capturada;
import dev.trilhajava.m13.aulas.padroes.EstadoDaCobranca.Estornada;
import dev.trilhajava.m13.aulas.padroes.EstadoDaCobranca.Pendente;
import dev.trilhajava.m13.aulas.padroes.EstadoDaCobranca.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoDaCobrancaTest {

    private static final Instant AGORA = Instant.parse("2026-10-06T13:00:00Z");

    @Test
    void caminhoFelizDePendenteAteEstornada() {
        EstadoDaCobranca capturada = new Pendente().autorizar("PF-0001").capturar(AGORA);

        assertThat(capturada).isEqualTo(new Capturada("PF-0001", AGORA));
        assertThat(capturada.estornar("cliente desistiu"))
                .isEqualTo(new Estornada("PF-0001", "cliente desistiu"));
    }

    @Test
    void naoCapturaOQueNaoFoiAutorizado() {
        assertThatThrownBy(() -> new Pendente().capturar(AGORA))
                .isInstanceOf(TransicaoInvalidaException.class)
                .hasMessage("não é possível capturar uma cobrança no estado Pendente");
    }
}
