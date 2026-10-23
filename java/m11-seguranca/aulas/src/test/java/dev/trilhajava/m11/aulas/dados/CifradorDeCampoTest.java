package dev.trilhajava.m11.aulas.dados;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CifradorDeCampoTest {

    KmsEmMemoria kms = new KmsEmMemoria();
    ChaveiroDeDados chaveiro = new ChaveiroDeDados(Map.of(1, kms.gerarChaveDeDados().emClaro()), 1);
    CifradorDeCampo cifrador = new CifradorDeCampo(chaveiro);

    @Test
    void cifraEDecifra() {
        String armazenado = cifrador.cifrar("12345678909", "paciente.cpf");

        assertThat(armazenado).startsWith("v1:").doesNotContain("12345678909");
        assertThat(cifrador.decifrar(armazenado, "paciente.cpf")).isEqualTo("12345678909");
    }

    @Test
    void mesmoTextoGeraCifradosDiferentes() {
        String primeiro = cifrador.cifrar("12345678909", "paciente.cpf");
        String segundo = cifrador.cifrar("12345678909", "paciente.cpf");

        assertThat(primeiro).isNotEqualTo(segundo);   // nonce novo a cada chamada
        assertThat(cifrador.decifrar(primeiro, "paciente.cpf"))
                .isEqualTo(cifrador.decifrar(segundo, "paciente.cpf"));
    }

    @Test
    void tamanhoDoValorGravado() {
        String armazenado = cifrador.cifrar("12345678909", "paciente.cpf");

        // 12 (nonce) + 11 (CPF) + 16 (tag) = 39 bytes -> 52 caracteres em Base64, mais "v1:"
        assertThat(armazenado).hasSize(55);
    }
}
