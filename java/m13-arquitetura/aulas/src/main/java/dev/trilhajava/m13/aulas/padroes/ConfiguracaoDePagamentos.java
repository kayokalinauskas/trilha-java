package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Boleto;
import dev.trilhajava.m13.aulas.padroes.fornecedor.PagaFacilClient;
import dev.trilhajava.m13.aulas.padroes.fornecedor.PagaFacilSandbox;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Onde os padrões são montados. Repare que só existe UM bean do tipo GatewayDePagamento:
 * o adaptador e os decorators são criados aqui dentro, na ordem certa, e o resto da
 * aplicação recebe a pilha pronta.
 */
@Configuration
@EnableCaching
@Import({EmissorDeNotaFiscal.class, ProgramaDeFidelidade.class})
public class ConfiguracaoDePagamentos {

    private static final System.Logger LOG = System.getLogger(ConfiguracaoDePagamentos.class.getName());

    @Bean
    PagaFacilClient pagaFacil() {
        return new PagaFacilSandbox(0);
    }

    @Bean
    GatewayDePagamento gatewayDePagamento(PagaFacilClient pagaFacil) {
        GatewayDePagamento adaptador = new AdaptadorPagaFacil(pagaFacil);
        GatewayDePagamento comRetentativa = new GatewayComRetentativa(adaptador, 3, Duration.ofMillis(200));
        return new GatewayComRegistro(comRetentativa, mensagem -> LOG.log(System.Logger.Level.INFO, mensagem));
    }

    @Bean
    @Order(1)
    RegraAntifraude boletoAteCincoMil() {
        BigDecimal limite = new BigDecimal("5000.00");
        return s -> s.meio() instanceof Boleto && s.valor().compareTo(limite) > 0
                ? Optional.of("boleto acima de R$ 5.000,00")
                : Optional.empty();
    }

    @Bean
    @Order(2)
    RegraAntifraude clientesBloqueados() {
        Set<String> bloqueados = Set.of("cliente-666");
        return s -> bloqueados.contains(s.clienteId())
                ? Optional.of("cliente bloqueado")
                : Optional.empty();
    }

    @Bean
    AnaliseAntifraude analiseAntifraude(List<RegraAntifraude> regras) {
        return new AnaliseAntifraude(regras); // o Spring entrega a lista na ordem do @Order
    }

    @Bean
    ServicoDeCheckout servicoDeCheckout(AnaliseAntifraude antifraude, GatewayDePagamento gateway,
                                        ApplicationEventPublisher eventos) {
        return new ServicoDeCheckout(antifraude, gateway, eventos);
    }

    @Bean
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("taxas");
    }

    @Bean
    TabelaDeTaxas tabelaDeTaxas() {
        return new TabelaDeTaxas();
    }
}
