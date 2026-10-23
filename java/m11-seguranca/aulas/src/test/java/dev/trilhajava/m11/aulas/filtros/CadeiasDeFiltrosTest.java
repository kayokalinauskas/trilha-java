package dev.trilhajava.m11.aulas.filtros;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.Filter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

/** Confere a estrutura: quantas cadeias, em que ordem, com quais filtros. */
@SpringBootTest
class CadeiasDeFiltrosTest {

    @Autowired
    FilterChainProxy filterChainProxy;

    @Test
    void haDuasCadeiasEAApiVemPrimeiro() {
        List<SecurityFilterChain> cadeias = filterChainProxy.getFilterChains();

        cadeias.forEach(cadeia -> System.out.println(cadeia.getFilters().stream()
                .map(filtro -> filtro.getClass().getSimpleName()).toList()));

        assertThat(cadeias).hasSize(2);
        assertThat(nomes(filterChainProxy.getFilters("/api/consultas"))).contains("BasicAuthenticationFilter");
        assertThat(nomes(filterChainProxy.getFilters("/admin/painel"))).contains("UsernamePasswordAuthenticationFilter");
    }

    @Test
    void aApiNaoTemCsrfNemLoginPorFormulario() {
        List<String> api = nomes(filterChainProxy.getFilters("/api/consultas"));

        assertThat(api).doesNotContain("CsrfFilter", "UsernamePasswordAuthenticationFilter");
        assertThat(nomes(filterChainProxy.getFilters("/admin/painel"))).contains("CsrfFilter");
    }

    @Test
    void aAuditoriaFicaEntreAAutenticacaoEOExceptionTranslationFilter() {
        List<String> api = nomes(filterChainProxy.getFilters("/api/consultas"));

        int basic = api.indexOf("BasicAuthenticationFilter");
        int auditoria = api.indexOf("FiltroDeAuditoria");
        int traducao = api.indexOf("ExceptionTranslationFilter");

        assertThat(auditoria).isGreaterThan(basic).isEqualTo(traducao - 1);
    }

    private static List<String> nomes(List<Filter> filtros) {
        return filtros.stream().map(filtro -> filtro.getClass().getSimpleName()).toList();
    }
}
