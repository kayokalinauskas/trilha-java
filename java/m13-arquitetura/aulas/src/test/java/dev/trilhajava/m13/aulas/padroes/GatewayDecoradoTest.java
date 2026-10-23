package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Pix;
import dev.trilhajava.m13.aulas.padroes.fornecedor.PagaFacilSandbox;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayDecoradoTest {

    @Test
    void repeteAsFalhasTemporariasERegistraUmaVezSo() {
        PagaFacilSandbox sandbox = new PagaFacilSandbox(2);
        List<String> registro = new ArrayList<>();
        GatewayDePagamento gateway = new GatewayComRegistro(
                new GatewayComRetentativa(new AdaptadorPagaFacil(sandbox), 3, Duration.ZERO),
                registro::add);

        ResultadoDaCobranca resultado = gateway.cobrar(SolicitacaoDeCobranca
                .builder("PED-4001", "cliente-42", new BigDecimal("79.90"), new Pix("loja@exemplo.com.br"))
                .build());

        assertThat(resultado).isEqualTo(new ResultadoDaCobranca.Aprovada("PF-0001"));
        assertThat(sandbox.chamadasRecebidas()).isEqualTo(3);
        assertThat(sandbox.transacoesCriadas()).isEqualTo(1);
        assertThat(registro).hasSize(2); // o registro está POR FORA da retentativa
    }
}
