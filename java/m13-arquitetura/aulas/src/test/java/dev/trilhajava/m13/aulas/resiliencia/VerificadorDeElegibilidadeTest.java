package dev.trilhajava.m13.aulas.resiliencia;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificadorDeElegibilidadeTest {

    private final PoliticaDeResiliencia politica = PoliticaDeResiliencia.paraTestes();
    private OperadoraFalsa operadora;
    private VerificadorDeElegibilidade verificador;

    @BeforeEach
    void subirOperadora() {
        operadora = OperadoraFalsa.iniciar();
        var cliente = new ClienteDaOperadora(operadora.url(), politica.timeoutDeConexao(), politica.timeoutDeLeitura());
        verificador = new VerificadorDeElegibilidade(cliente, politica);
    }

    @AfterEach
    void derrubarOperadora() {
        operadora.close();
    }

    @Test
    void doisErros503SeguidosSaoAbsorvidosPelasRepeticoes() {
        operadora.falharAsProximas(2);

        Elegibilidade resultado = verificador.verificar("89001234");

        assertThat(resultado.situacao()).isEqualTo(Elegibilidade.Situacao.ATIVA);
        assertThat(operadora.chamadasRecebidas()).isEqualTo(3);   // 1 tentativa + 2 repetições
    }

    @Test
    void carteirinhaDesconhecidaNaoERepetidaNemAbreOCircuito() {
        for (int i = 0; i < 6; i++) {
            assertThatThrownBy(() -> verificador.verificar("00012345"))
                    .isInstanceOf(CarteirinhaDesconhecidaException.class);
        }

        assertThat(operadora.chamadasRecebidas()).isEqualTo(6);   // uma por consulta, nenhuma repetição
        assertThat(verificador.estadoDoCircuito()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void operadoraForaAbreOCircuitoEOFallbackNaoInventaResultado() {
        operadora.fora(true);
        for (int i = 0; i < 4; i++) {                                // enche a janela de 4 chamadas
            assertThat(verificador.verificar("89001234").situacao())
                    .isEqualTo(Elegibilidade.Situacao.NAO_CONFIRMADA);
        }
        int chamadasAteAbrir = operadora.chamadasRecebidas();

        Elegibilidade comCircuitoAberto = verificador.verificar("89001234");

        assertThat(chamadasAteAbrir).isEqualTo(12);                  // 4 consultas × 3 tentativas
        assertThat(verificador.estadoDoCircuito()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(comCircuitoAberto.situacao()).isEqualTo(Elegibilidade.Situacao.NAO_CONFIRMADA);
        assertThat(comCircuitoAberto.plano()).isNull();
        assertThat(operadora.chamadasRecebidas()).isEqualTo(chamadasAteAbrir); // nem tocou na operadora
    }
}
