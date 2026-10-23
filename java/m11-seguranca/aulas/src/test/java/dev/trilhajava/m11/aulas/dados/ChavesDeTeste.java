package dev.trilhajava.m11.aulas.dados;

import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * As chaves dos testes JPA: a v1 (antiga) e a v2 (atual) vêm do KMS em memória; a chave do blind
 * index é outra DEK, separada da que cifra.
 */
@TestConfiguration
class ChavesDeTeste {

    static final KmsEmMemoria KMS = new KmsEmMemoria();
    static final SecretKey V1 = KMS.gerarChaveDeDados().emClaro();
    static final SecretKey V2 = KMS.gerarChaveDeDados().emClaro();
    static final SecretKey HMAC = KMS.gerarChaveDeDados().emClaro();

    @Bean
    CifradorDeCampo cifradorDeCampo() {
        return new CifradorDeCampo(new ChaveiroDeDados(Map.of(1, V1, 2, V2), 2));
    }

    @Bean
    IndiceCego indiceCego() {
        return new IndiceCego(HMAC);
    }

    /** O cifrador como era antes da rotação, para montar dados "antigos" nos testes. */
    static CifradorDeCampo cifradorAntesDaRotacao() {
        return new CifradorDeCampo(new ChaveiroDeDados(Map.of(1, V1), 1));
    }
}
