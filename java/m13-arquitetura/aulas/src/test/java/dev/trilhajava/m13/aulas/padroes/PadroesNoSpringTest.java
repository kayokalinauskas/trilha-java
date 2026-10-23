package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Boleto;
import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Pix;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(ConfiguracaoDePagamentos.class)
class PadroesNoSpringTest {

    @Autowired ServicoDeCheckout checkout;
    @Autowired EmissorDeNotaFiscal notasFiscais;
    @Autowired ProgramaDeFidelidade fidelidade;
    @Autowired TabelaDeTaxas taxas;

    @Test
    void pagamentoAprovadoAvisaOsOuvintesERecusaNaoAvisa() {
        checkout.pagar(SolicitacaoDeCobranca
                .builder("PED-6001", "cliente-7", new BigDecimal("150.75"), new Pix("loja@exemplo.com.br"))
                .build());
        ResultadoDaCobranca recusa = checkout.pagar(SolicitacaoDeCobranca
                .builder("PED-6002", "cliente-7", new BigDecimal("7500.00"), new Boleto(LocalDate.of(2026, 10, 20)))
                .build());

        assertThat(recusa).isEqualTo(new ResultadoDaCobranca.Recusada("antifraude: boleto acima de R$ 5.000,00"));
        assertThat(notasFiscais.notasEmitidas()).containsExactly("NF do pedido PED-6001: R$ 150.75");
        assertThat(fidelidade.pontosDe("cliente-7")).isEqualTo(150);
    }

    @Test
    void chamadaInternaNaoPassaPeloProxyDoCache() {
        assertThat(AopUtils.isAopProxy(taxas)).isTrue();

        taxas.percentualVigente("PIX");
        taxas.percentualVigente("PIX");
        assertThat(taxas.consultasAoBanco()).isEqualTo(1); // a segunda veio do cache

        taxas.simular(List.of("PIX", "PIX", "PIX"));
        assertThat(taxas.consultasAoBanco()).isEqualTo(4); // this.percentualVigente ignorou o cache
    }
}
