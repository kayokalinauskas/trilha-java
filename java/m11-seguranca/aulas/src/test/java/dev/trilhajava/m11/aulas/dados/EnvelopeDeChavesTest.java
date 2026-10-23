package dev.trilhajava.m11.aulas.dados;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvelopeDeChavesTest {

    @Test
    void chaveiroCarregadoDasChavesEmbrulhadas() {
        KmsEmMemoria kms = new KmsEmMemoria();
        ServicoDeChaves.ChaveDeDadosGerada gerada = kms.gerarChaveDeDados();

        // O que vai para a configuração/banco é só a versão embrulhada (12 + 32 + 16 = 60 bytes).
        byte[] embrulhada = gerada.embrulhada();
        assertThat(embrulhada).hasSize(60);

        // Na subida, a aplicação pede ao KMS para abrir a DEK e monta o chaveiro.
        ChaveiroDeDados chaveiro = ChaveiroDeDados.carregar(kms, Map.of(1, embrulhada), 1);
        assertThat(chaveiro.atual().getEncoded()).isEqualTo(gerada.emClaro().getEncoded());

        CifradorDeCampo cifrador = new CifradorDeCampo(chaveiro);
        String cpf = cifrador.cifrar("12345678909", "paciente.cpf");
        assertThat(cifrador.decifrar(cpf, "paciente.cpf")).isEqualTo("12345678909");
    }

    @Test
    void outroKmsNaoAbreAChave() {
        byte[] embrulhada = new KmsEmMemoria().gerarChaveDeDados().embrulhada();
        KmsEmMemoria outraContaDeNuvem = new KmsEmMemoria();

        assertThatThrownBy(() -> outraContaDeNuvem.desembrulhar(embrulhada))
                .isInstanceOf(FalhaAoDecifrarException.class)
                .hasMessage("a chave de dados não pôde ser desembrulhada");
    }
}
