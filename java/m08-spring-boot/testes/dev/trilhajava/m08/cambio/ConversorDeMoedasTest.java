package dev.trilhajava.m08.cambio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = {
    "cambio.timeout=500ms",
    "cambio.validade=60s",
    "cambio.validade-maxima-fallback=24h"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // cada teste começa com o cache vazio
@DisplayName("Exercício 08.04 — Cliente HTTP de câmbio com cache, timeout e fallback")
class ConversorDeMoedasTest {

    static final ApiDeCotacoesFalsa API = new ApiDeCotacoesFalsa();

    @DynamicPropertySource
    static void urlDaApi(DynamicPropertyRegistry registro) {
        registro.add("cambio.base-url", API::url);
    }

    @TestConfiguration
    static class Relogio {
        @Bean
        @Primary
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste();
        }
    }

    @Autowired
    ConversorDeMoedas conversor;

    @Autowired
    RelogioDeTeste relogio;

    @BeforeEach
    void limpar() {
        API.limpar();
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    @DisplayName("converte reais usando a taxa de VENDA, com 2 casas (HALF_EVEN)")
    void converte() {
        API.cotacao("USD", "5.1234", "5.1301");
        Conversao c = conversor.converter(bd("1000.00"), "USD");

        assertThat(c.moeda()).isEqualTo("USD");
        assertThat(c.valorEmReais()).isEqualByComparingTo("1000.00");
        assertThat(c.taxa()).isEqualByComparingTo("5.1301");
        assertThat(c.valorConvertido()).isEqualByComparingTo("194.93");
        assertThat(c.valorConvertido().scale()).isEqualTo(2);
        assertThat(c.fonte()).isEqualTo(FonteDaCotacao.API);
        assertThat(API.requisicoes()).containsExactly("GET /cotacoes/USD");
    }

    @Test
    @DisplayName("normaliza o código da moeda (\" eur \" → EUR)")
    void normaliza() {
        API.cotacao("EUR", "5.90", "6.00");
        assertThat(conversor.converter(bd("60.00"), " eur ").valorConvertido()).isEqualByComparingTo("10.00");
        assertThat(API.requisicoes()).containsExactly("GET /cotacoes/EUR");
    }

    @Test
    @DisplayName("dentro da validade (60s), reaproveita a cotação sem chamar a API")
    void cache() {
        API.cotacao("USD", "5.00", "5.00");
        conversor.converter(bd("100"), "USD");
        relogio.avancar(Duration.ofSeconds(60));
        Conversao segunda = conversor.converter(bd("50"), "USD");

        assertThat(segunda.fonte()).isEqualTo(FonteDaCotacao.CACHE);
        assertThat(segunda.valorConvertido()).isEqualByComparingTo("10.00");
        assertThat(API.requisicoes()).hasSize(1);
    }

    @Test
    @DisplayName("depois da validade, busca de novo e usa a taxa nova")
    void expira() {
        API.cotacao("USD", "5.00", "5.00");
        conversor.converter(bd("100"), "USD");
        relogio.avancar(Duration.ofSeconds(61));
        API.cotacao("USD", "4.00", "4.00");
        Conversao segunda = conversor.converter(bd("100"), "USD");

        assertThat(segunda.fonte()).isEqualTo(FonteDaCotacao.API);
        assertThat(segunda.valorConvertido()).isEqualByComparingTo("25.00");
        assertThat(API.requisicoes()).hasSize(2);
    }

    @Test
    @DisplayName("404 da API → MoedaNaoSuportadaException")
    void moedaDesconhecida() {
        assertThatThrownBy(() -> conversor.converter(bd("10"), "XYZ"))
                .isInstanceOf(MoedaNaoSuportadaException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    @DisplayName("API com erro e sem cotação anterior → CotacaoIndisponivelException")
    void indisponivelSemCache() {
        API.falha("USD", 503);
        assertThatThrownBy(() -> conversor.converter(bd("10"), "USD"))
                .isInstanceOf(CotacaoIndisponivelException.class);
    }

    @Test
    @DisplayName("API com erro e cotação vencida há menos de 24h → usa a antiga (CACHE_EXPIRADO)")
    void fallback() {
        API.cotacao("USD", "5.00", "5.00");
        conversor.converter(bd("100"), "USD");
        relogio.avancar(Duration.ofHours(2));
        API.falha("USD", 500);

        Conversao c = conversor.converter(bd("100"), "USD");
        assertThat(c.fonte()).isEqualTo(FonteDaCotacao.CACHE_EXPIRADO);
        assertThat(c.valorConvertido()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("API com erro e cotação com mais de 24h → CotacaoIndisponivelException")
    void fallbackVencido() {
        API.cotacao("USD", "5.00", "5.00");
        conversor.converter(bd("100"), "USD");
        relogio.avancar(Duration.ofHours(25));
        API.falha("USD", 500);

        assertThatThrownBy(() -> conversor.converter(bd("100"), "USD"))
                .isInstanceOf(CotacaoIndisponivelException.class);
    }

    @Test
    @DisplayName("API lenta: desiste no timeout configurado (500ms) em vez de ficar esperando")
    void timeout() {
        API.lenta("GBP", 3_000);
        long inicio = System.nanoTime();
        assertThatThrownBy(() -> conversor.converter(bd("10"), "GBP"))
                .isInstanceOf(CotacaoIndisponivelException.class);
        long ms = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
        assertThat(ms).as("tempo até desistir (ms)").isLessThan(2_000);
    }

    @Test
    @DisplayName("valor zero, negativo ou nulo → IllegalArgumentException, sem chamar a API")
    void valorInvalido() {
        assertThatThrownBy(() -> conversor.converter(bd("0"), "USD")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> conversor.converter(bd("-5"), "USD")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> conversor.converter(null, "USD")).isInstanceOf(IllegalArgumentException.class);
        assertThat(API.requisicoes()).isEmpty();
    }
}
