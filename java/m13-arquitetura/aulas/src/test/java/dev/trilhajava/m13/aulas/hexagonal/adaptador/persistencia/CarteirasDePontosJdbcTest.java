package dev.trilhajava.m13.aulas.hexagonal.adaptador.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CarteiraAlteradaException;
import dev.trilhajava.m13.aulas.hexagonal.dominio.CarteiraDePontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Pontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Recompensa;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

/** Teste de fatia: só o adaptador, contra um banco de verdade (H2 em memória). */
@JdbcTest(properties = {
        "spring.sql.init.schema-locations=classpath:hexagonal/schema.sql",
        "spring.sql.init.data-locations=classpath:hexagonal/data.sql"})
@Import(CarteirasDePontosJdbc.class)
class CarteirasDePontosJdbcTest {

    @Autowired
    CarteirasDePontosJdbc carteiras;

    @Test
    void gravaONovoSaldoERecusaGravacaoComVersaoVelha() {
        CarteiraDePontos primeira = carteiras.doCliente("cli-ana").orElseThrow();
        CarteiraDePontos segunda = carteiras.doCliente("cli-ana").orElseThrow();   // leitura concorrente
        Recompensa cafe = new Recompensa("CAFE-GRATIS", "Um café", Pontos.de(500), true);

        primeira.resgatar(cafe, Instant.now());
        carteiras.salvar(primeira);

        CarteiraDePontos relida = carteiras.doCliente("cli-ana").orElseThrow();
        assertThat(relida.saldo()).isEqualTo(Pontos.de(700));
        assertThat(relida.versao()).isEqualTo(1);

        segunda.resgatar(cafe, Instant.now());
        assertThatThrownBy(() -> carteiras.salvar(segunda))
                .isInstanceOf(CarteiraAlteradaException.class);
    }
}
