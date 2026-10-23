package dev.trilhajava.m12.aulas.idempotencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Idempotency-Key na API de transferências")
class GuardaDeIdempotenciaTest {

    @Test
    @DisplayName("a repetição devolve a mesma resposta sem transferir de novo; outro corpo com a mesma chave dá 422")
    void repeticaoDevolveAMesmaResposta() {
        var banco = new BancoDeTeste();
        var guarda = new GuardaDeIdempotencia(banco.jdbc, banco.transacao, BancoDeTeste.RELOGIO);
        String corpo = "{\"destino\":\"CT-BRUNO\",\"valor\":\"150.00\"}";

        var primeira = guarda.executar("CLI-ANA", "7f3c-pix-001", corpo, () -> transferir(banco, "CT-BRUNO", "150.00"));
        var repetida = guarda.executar("CLI-ANA", "7f3c-pix-001", corpo, () -> transferir(banco, "CT-BRUNO", "150.00"));
        var outroCorpo = guarda.executar("CLI-ANA", "7f3c-pix-001", corpo.replace("150.00", "990.00"),
            () -> transferir(banco, "CT-BRUNO", "990.00"));

        assertThat(primeira.status()).isEqualTo(201);
        assertThat(repetida).isEqualTo(primeira);
        assertThat(outroCorpo.status()).isEqualTo(422);
        assertThat(banco.jdbc.sql("SELECT COUNT(*) FROM transferencia").query(Long.class).single()).isEqualTo(1L);
    }

    private static RespostaHttp transferir(BancoDeTeste banco, String destino, String valor) {
        String id = "TRF-" + UUID.randomUUID().toString().substring(0, 8);
        banco.jdbc.sql("INSERT INTO transferencia (id, cliente_id, destino, valor) VALUES (?, 'CLI-ANA', ?, ?)")
            .params(id, destino, new BigDecimal(valor))
            .update();
        return new RespostaHttp(201, "{\"id\":\"" + id + "\",\"status\":\"CONCLUIDA\"}");
    }
}
