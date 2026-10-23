package dev.trilhajava.fintech.dominio;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Transferência (regras de domínio)")
class TransferenciaTest {

    static final UUID A = UUID.randomUUID();
    static final UUID B = UUID.randomUUID();

    @Test
    @DisplayName("origem e destino iguais → TransferenciaInvalidaException")
    void mesmaConta() {
        assertThatThrownBy(() -> new Transferencia(UUID.randomUUID(), A, A, BigDecimal.TEN, Instant.EPOCH, "k"))
                .isInstanceOf(TransferenciaInvalidaException.class);
    }

    @Test
    @DisplayName("o mesmo pedido (30 == 30.00) é reconhecido; outro conteúdo com a mesma chave é recusado")
    void impressaoDigital() {
        var t = new Transferencia(UUID.randomUUID(), A, B, new BigDecimal("30.00"), Instant.EPOCH, "k");

        assertThatCode(() -> t.exigirMesmoPedido(A, B, new BigDecimal("30"))).doesNotThrowAnyException();
        assertThatThrownBy(() -> t.exigirMesmoPedido(A, B, new BigDecimal("31")))
                .isInstanceOf(ChaveIdempotenciaReutilizadaException.class);
        assertThatThrownBy(() -> t.exigirMesmoPedido(B, A, new BigDecimal("30")))
                .isInstanceOf(ChaveIdempotenciaReutilizadaException.class);
    }
}
