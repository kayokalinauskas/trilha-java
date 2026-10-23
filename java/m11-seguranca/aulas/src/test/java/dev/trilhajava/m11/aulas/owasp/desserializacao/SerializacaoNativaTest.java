package dev.trilhajava.m11.aulas.owasp.desserializacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class SerializacaoNativaTest {

    static byte[] serializar(Object objeto) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var saida = new ObjectOutputStream(bytes)) {
            saida.writeObject(objeto);
        }
        return bytes.toByteArray();
    }

    static final CarrinhoSalvo CARRINHO = new CarrinhoSalvo("cliente-42", List.of("CAFE-1KG", "FILTRO-102"),
            new BigDecimal("112.30"));

    @Test
    void carrinhoLegitimoPassaPeloFiltro() throws Exception {
        assertThat(CarrinhoSalvo.lerComFiltro(serializar(CARRINHO))).isEqualTo(CARRINHO);
    }

    @Test
    void semFiltroQualquerClasseSerializableNasce() throws Exception {
        var intrusa = new ConfiguracaoInterna();
        intrusa.modoAdministrador = true;

        // O código esperava um carrinho; o readObject() já criou o objeto antes de qualquer cast.
        assertThat(CarrinhoSalvo.lerSemFiltro(serializar(intrusa))).isInstanceOf(ConfiguracaoInterna.class);
    }

    @Test
    void comFiltroAClasseIntrusaEhRecusadaAntesDeSerCriada() {
        assertThatThrownBy(() -> CarrinhoSalvo.lerComFiltro(serializar(new ConfiguracaoInterna())))
                .isInstanceOf(InvalidClassException.class)
                .hasMessageContaining("filter status: REJECTED");
    }
}
