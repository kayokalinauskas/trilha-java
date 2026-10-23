package dev.trilhajava.m11.aulas.dados;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class RotacaoDeChaveTest {

    KmsEmMemoria kms = new KmsEmMemoria();
    SecretKey v1 = kms.gerarChaveDeDados().emClaro();
    SecretKey v2 = kms.gerarChaveDeDados().emClaro();

    @Test
    void depoisDaRotacaoLeOAntigoEGravaComONovo() {
        // Antes: só existe a v1.
        CifradorDeCampo antes = new CifradorDeCampo(new ChaveiroDeDados(Map.of(1, v1), 1));
        String gravadoNoAnoPassado = antes.cifrar("12345678909", "paciente.cpf");

        // Rotação: a v2 entra e vira a atual; a v1 continua no chaveiro para leitura.
        CifradorDeCampo depois = new CifradorDeCampo(new ChaveiroDeDados(Map.of(1, v1, 2, v2), 2));

        assertThat(depois.decifrar(gravadoNoAnoPassado, "paciente.cpf")).isEqualTo("12345678909");
        assertThat(depois.precisaRecifrar(gravadoNoAnoPassado)).isTrue();

        String regravado = depois.recifrar(gravadoNoAnoPassado, "paciente.cpf");
        assertThat(regravado).startsWith("v2:");
        assertThat(depois.precisaRecifrar(regravado)).isFalse();
        assertThat(depois.decifrar(regravado, "paciente.cpf")).isEqualTo("12345678909");
    }

    @Test
    void valoresNovosJaSaemComAVersaoAtual() {
        CifradorDeCampo depois = new CifradorDeCampo(new ChaveiroDeDados(Map.of(1, v1, 2, v2), 2));

        assertThat(depois.cifrar("98765432100", "paciente.cpf")).startsWith("v2:");
    }
}
