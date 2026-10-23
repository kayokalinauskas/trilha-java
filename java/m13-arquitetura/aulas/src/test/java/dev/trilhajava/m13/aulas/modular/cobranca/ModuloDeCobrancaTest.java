package dev.trilhajava.m13.aulas.modular.cobranca;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m13.aulas.modular.agenda.eventos.ConsultaRealizada;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

/** Sobe só o módulo cobranca: nem a agenda nem as notificações estão no contexto. */
@ApplicationModuleTest
class ModuloDeCobrancaTest {

    @Autowired
    Cobrancas cobrancas;

    @Test
    void consultaRealizadaGeraCobranca(Scenario cenario) {
        var consulta = UUID.randomUUID();
        var evento = new ConsultaRealizada(consulta, "Marina Couto", "retorno de cardiologia",
                new BigDecimal("180.00"));

        cenario.publish(evento)
                .andWaitForStateChange(() -> cobrancas.daConsulta(consulta))
                .andVerify(cobranca -> {
                    assertThat(cobranca).get().extracting(Cobranca::valor).isEqualTo(new BigDecimal("180.00"));
                    assertThat(cobranca).get().extracting(Cobranca::autorizacao).asString().startsWith("AUT-");
                });
    }
}
