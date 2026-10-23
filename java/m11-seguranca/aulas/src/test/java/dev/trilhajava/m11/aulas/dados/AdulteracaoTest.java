package dev.trilhajava.m11.aulas.dados;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import java.util.Map;
import javax.crypto.AEADBadTagException;
import org.junit.jupiter.api.Test;

class AdulteracaoTest {

    KmsEmMemoria kms = new KmsEmMemoria();
    ChaveiroDeDados chaveiro = new ChaveiroDeDados(Map.of(1, kms.gerarChaveDeDados().emClaro()), 1);
    CifradorDeCampo cifrador = new CifradorDeCampo(chaveiro);

    @Test
    void umBitTrocadoNoCifradoEDetectado() {
        String armazenado = cifrador.cifrar("12345678909", "paciente.cpf");
        byte[] pacote = Base64.getDecoder().decode(armazenado.substring(3));
        pacote[20] ^= 1;   // um único bit, no meio do texto cifrado
        String adulterado = "v1:" + Base64.getEncoder().encodeToString(pacote);

        assertThatThrownBy(() -> cifrador.decifrar(adulterado, "paciente.cpf"))
                .isInstanceOf(FalhaAoDecifrarException.class)
                .hasMessage("não foi possível decifrar paciente.cpf")
                .cause()
                .isInstanceOf(AEADBadTagException.class)
                .hasMessage("Tag mismatch");
    }

    @Test
    void valorCopiadoParaOutroCampoNaoAbre() {
        String cpfDoPaciente = cifrador.cifrar("12345678909", "paciente.cpf");

        // alguém com acesso ao banco copia a coluna para o CPF de outra tabela
        assertThatThrownBy(() -> cifrador.decifrar(cpfDoPaciente, "medico.cpf"))
                .isInstanceOf(FalhaAoDecifrarException.class)
                .cause()
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void versaoDeChaveDesconhecidaFalhaAlto() {
        String armazenado = cifrador.cifrar("12345678909", "paciente.cpf");
        String comVersaoTrocada = "v7" + armazenado.substring(2);

        assertThatThrownBy(() -> cifrador.decifrar(comVersaoTrocada, "paciente.cpf"))
                .isInstanceOf(FalhaAoDecifrarException.class)
                .hasMessage("versão de chave desconhecida: v7");
    }
}
