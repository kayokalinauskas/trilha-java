package dev.trilhajava.m11.aulas.dados;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class IndiceCegoTest {

    IndiceCego indice = new IndiceCego(chave((byte) 7));

    @Test
    void mesmoCpfComOuSemPontuacaoGeraOMesmoIndice() {
        String formatado = indice.doCpf(new Cpf("123.456.789-09"), "paciente.cpf");
        String soDigitos = indice.doCpf(new Cpf("12345678909"), "paciente.cpf");

        assertThat(formatado).isEqualTo(soDigitos).hasSize(64);   // HMAC-SHA256 em hexadecimal
    }

    @Test
    void cpfsDiferentesGeramIndicesDiferentes() {
        assertThat(indice.doCpf(new Cpf("12345678909"), "paciente.cpf"))
                .isNotEqualTo(indice.doCpf(new Cpf("12345678910"), "paciente.cpf"));
    }

    @Test
    void contextoSeparaOsCampos() {
        assertThat(indice.doCpf(new Cpf("12345678909"), "paciente.cpf"))
                .isNotEqualTo(indice.doCpf(new Cpf("12345678909"), "medico.cpf"));
    }

    @Test
    void semAChaveNaoSeReproduzOIndice() {
        IndiceCego deQuemRoubouOBanco = new IndiceCego(chave((byte) 8));

        assertThat(deQuemRoubouOBanco.doCpf(new Cpf("12345678909"), "paciente.cpf"))
                .isNotEqualTo(indice.doCpf(new Cpf("12345678909"), "paciente.cpf"));
    }

    private static SecretKeySpec chave(byte preenchimento) {
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, preenchimento);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
