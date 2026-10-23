package dev.trilhajava.m11.aulas.owasp.desserializacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.exc.InvalidDefinitionException;
import tools.jackson.databind.exc.InvalidTypeIdException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

class JacksonPolimorfismoTest {

    // O cliente escolhe a classe: um tipo interno, com um campo que ninguém deveria poder ligar.
    static final String EVENTO_MALICIOSO = """
            {"pedido": "PED-100",
             "detalhes": {"@class": "dev.trilhajava.m11.aulas.owasp.desserializacao.ConfiguracaoInterna",
                          "chave": "pagamentos", "modoAdministrador": true}}
            """;

    @Test
    void jackson3PorPadraoRecusaTipoPorClasseEmCampoObject() {
        var mapper = JsonMapper.builder().build();

        assertThatThrownBy(() -> mapper.readValue(EVENTO_MALICIOSO, EventoComTipoLivre.class))
                .isInstanceOf(InvalidDefinitionException.class)
                .hasMessageContaining("denies resolution of all subtypes of base type `java.lang.Object`");
    }

    @Test
    void validadorPermissivoDeixaOClienteInstanciarQualquerClasse() {
        // O "conserto" que aparece em fóruns quando o erro acima surge. É exatamente o que não fazer.
        var mapper = JsonMapper.builder()
                .polymorphicTypeValidator(BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build())
                .build();

        var evento = mapper.readValue(EVENTO_MALICIOSO, EventoComTipoLivre.class);

        assertThat(evento.detalhes).isInstanceOf(ConfiguracaoInterna.class);
        assertThat(((ConfiguracaoInterna) evento.detalhes).modoAdministrador).isTrue();
    }

    @Test
    void nomeLogicoComSubtiposFechadosFunciona() {
        var mapper = JsonMapper.builder().build();

        MeioDePagamento meio = mapper.readValue("""
                {"tipo": "pix", "chave": "ana@exemplo.com", "valor": 112.30}""", MeioDePagamento.class);

        assertThat(meio).isEqualTo(new MeioDePagamento.Pix("ana@exemplo.com", new BigDecimal("112.30")));
    }

    @Test
    void nomeLogicoRecusaTipoDesconhecidoENomeDeClasse() {
        var mapper = JsonMapper.builder().build();

        assertThatThrownBy(() -> mapper.readValue("""
                {"tipo": "boleto", "valor": 10}""", MeioDePagamento.class))
                .isInstanceOf(InvalidTypeIdException.class)
                .hasMessageContaining("Could not resolve type id 'boleto'")
                .hasMessageContaining("known type ids = [cartao, pix]");

        assertThatThrownBy(() -> mapper.readValue("""
                {"tipo": "dev.trilhajava.m11.aulas.owasp.desserializacao.ConfiguracaoInterna"}""",
                MeioDePagamento.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }
}
