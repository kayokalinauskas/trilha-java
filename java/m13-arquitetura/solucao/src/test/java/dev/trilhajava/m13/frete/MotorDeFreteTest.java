package dev.trilhajava.m13.frete;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import dev.trilhajava.m13.frete.legado.CalculadoraDeFreteLegada;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = FreteApplication.class)
@DisplayName("Exercício 13.01 — motor de frete (Strategy e aberto/fechado)")
class MotorDeFreteTest {

    static final String SP_CAPITAL = "01310100";
    static final String CAMPINAS = "13015000";
    static final String CURITIBA = "80010000";
    static final String RECIFE = "50010000";

    @Autowired
    MotorDeFrete motor;

    static Pedido pedido(String cep, String valor, int gramas, boolean fragil) {
        return new Pedido(cep, new BigDecimal(valor), gramas, fragil);
    }

    Optional<Cotacao> cotacao(Pedido pedido, String modalidade) {
        return motor.cotar(pedido).stream().filter(c -> c.modalidade().equals(modalidade)).findFirst();
    }

    static Cotacao de(String modalidade, String valor, int prazo) {
        return new Cotacao(modalidade, new BigDecimal(valor), prazo);
    }

    @Test
    @DisplayName("PAC: preço base da região até 1 kg")
    void pacBase() {
        assertThat(cotacao(pedido(CAMPINAS, "50.00", 1_000, false), "PAC")).contains(de("PAC", "18.90", 5));
        assertThat(cotacao(pedido(RECIFE, "50.00", 800, false), "PAC")).contains(de("PAC", "32.90", 10));
    }

    @Test
    @DisplayName("PAC: + R$ 2,50 por kg (ou fração) acima do primeiro")
    void pacExcedente() {
        // 3.200 g: 2.200 g acima de 1 kg = 3 frações
        assertThat(cotacao(pedido(CURITIBA, "50.00", 3_200, false), "PAC")).contains(de("PAC", "30.40", 6));
        assertThat(cotacao(pedido(CURITIBA, "50.00", 1_001, false), "PAC")).contains(de("PAC", "25.40", 6));
    }

    @Test
    @DisplayName("SEDEX: o PAC × 1,8, em metade do prazo (arredondada para cima)")
    void sedex() {
        assertThat(cotacao(pedido(CAMPINAS, "50.00", 1_000, false), "SEDEX")).contains(de("SEDEX", "34.02", 3));
        assertThat(cotacao(pedido(RECIFE, "50.00", 1_000, false), "SEDEX")).contains(de("SEDEX", "59.22", 5));
    }

    @Test
    @DisplayName("TRANSPORTADORA: só acima de 5 kg; R$ 45 + R$ 1,20 por kg; frágil +15%")
    void transportadora() {
        assertThat(cotacao(pedido(CAMPINAS, "50.00", 5_000, false), "TRANSPORTADORA")).isEmpty();
        assertThat(cotacao(pedido(CAMPINAS, "50.00", 12_500, false), "TRANSPORTADORA"))
                .contains(de("TRANSPORTADORA", "60.60", 7));
        assertThat(cotacao(pedido(CAMPINAS, "50.00", 12_500, true), "TRANSPORTADORA"))
                .contains(de("TRANSPORTADORA", "69.69", 7));
    }

    @Test
    @DisplayName("acima de 30 kg, PAC e SEDEX não atendem")
    void pesado() {
        List<Cotacao> cotacoes = motor.cotar(pedido(CURITIBA, "50.00", 30_001, false));
        assertThat(cotacoes).extracting(Cotacao::modalidade).containsExactly("TRANSPORTADORA");
    }

    @Test
    @DisplayName("RETIRADA: grátis em 1 dia, só na capital paulista")
    void retirada() {
        assertThat(cotacao(pedido(SP_CAPITAL, "50.00", 500, false), "RETIRADA")).contains(de("RETIRADA", "0.00", 1));
        assertThat(cotacao(pedido("05999999", "50.00", 500, false), "RETIRADA")).isPresent();
        assertThat(cotacao(pedido("06000000", "50.00", 500, false), "RETIRADA")).isEmpty();
        assertThat(cotacao(pedido("00999999", "50.00", 500, false), "RETIRADA")).isEmpty();
    }

    @Test
    @DisplayName("item frágil: + R$ 8,00 de seguro no PAC e no SEDEX (calculado sobre o PAC sem seguro)")
    void seguro() {
        Pedido fragil = pedido(CAMPINAS, "50.00", 1_000, true);
        assertThat(cotacao(fragil, "PAC")).contains(de("PAC", "26.90", 5));
        assertThat(cotacao(fragil, "SEDEX")).contains(de("SEDEX", "42.02", 3));
    }

    @Test
    @DisplayName("PAC grátis no Sudeste a partir de R$ 299,00 — inclusive o seguro")
    void freteGratis() {
        assertThat(cotacao(pedido(CAMPINAS, "299.00", 1_000, true), "PAC")).contains(de("PAC", "0.00", 5));
        assertThat(cotacao(pedido(CAMPINAS, "298.99", 1_000, false), "PAC")).contains(de("PAC", "18.90", 5));
        assertThat(cotacao(pedido(CURITIBA, "500.00", 1_000, false), "PAC")).contains(de("PAC", "22.90", 6));
        assertThat(cotacao(pedido(CAMPINAS, "299.00", 1_000, false), "SEDEX")).contains(de("SEDEX", "34.02", 3));
    }

    @Test
    @DisplayName("ordena por valor, depois prazo, depois nome")
    void ordem() {
        List<Cotacao> cotacoes = motor.cotar(pedido(SP_CAPITAL, "300.00", 6_000, false));
        assertThat(cotacoes).extracting(Cotacao::modalidade).containsExactly("RETIRADA", "PAC", "TRANSPORTADORA", "SEDEX");
    }

    @Test
    @DisplayName("golden master: 2.000 pedidos aleatórios dão exatamente o mesmo resultado que o código legado")
    void goldenMaster() {
        var legado = new CalculadoraDeFreteLegada();
        var aleatorio = new Random(2026);
        for (int i = 0; i < 2_000; i++) {
            String cep = String.format("%08d", aleatorio.nextInt(100_000_000));
            String valor = BigDecimal.valueOf(aleatorio.nextInt(60_000), 2).toPlainString();
            Pedido p = pedido(cep, valor, 1 + aleatorio.nextInt(40_000), aleatorio.nextBoolean());
            assertThat(motor.cotar(p)).as("pedido %s", p).isEqualTo(legado.cotar(p));
        }
    }

    @Test
    @DisplayName("arquitetura: há modalidades e promoções, e o motor não conhece nenhuma delas (nem o legado)")
    void arquitetura() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.trilhajava.m13.frete");
        List<JavaClass> modalidades = classes.stream()
                .filter(c -> !c.isInterface() && c.isAssignableTo(ModalidadeDeFrete.class)).toList();
        List<JavaClass> promocoes = classes.stream()
                .filter(c -> !c.isInterface() && c.isAssignableTo(RegraDePromocao.class)).toList();
        assertThat(modalidades).as("implementações de ModalidadeDeFrete").hasSizeGreaterThanOrEqualTo(4);
        assertThat(promocoes).as("implementações de RegraDePromocao").hasSizeGreaterThanOrEqualTo(2);

        noClasses().that().haveSimpleName("MotorDeFrete")
                .should().dependOnClassesThat().implement(ModalidadeDeFrete.class)
                .orShould().dependOnClassesThat().implement(RegraDePromocao.class)
                .check(classes);
        noClasses().that().resideOutsideOfPackage("..legado..")
                .should().dependOnClassesThat().resideInAPackage("..legado..")
                .check(classes);
    }

    @Nested
    @Import(MotorDeFreteTest.Drone.Config.class)
    @DisplayName("aberto para extensão")
    class Drone {

        @TestConfiguration(proxyBeanMethods = false)
        static class Config {
            @Bean
            ModalidadeDeFrete drone() {
                return pedido -> pedido.pesoEmGramas() <= 2_000
                        ? Optional.of(new Cotacao("DRONE", new BigDecimal("9.99"), 0))
                        : Optional.empty();
            }
        }

        @Test
        @DisplayName("uma modalidade nova é só um bean novo: o motor a usa sem nenhuma mudança")
        void novaModalidade() {
            List<Cotacao> cotacoes = motor.cotar(pedido(CAMPINAS, "50.00", 1_500, false));
            assertThat(cotacoes).extracting(Cotacao::modalidade).contains("DRONE", "PAC", "SEDEX");
            assertThat(cotacoes.getFirst()).isEqualTo(de("DRONE", "9.99", 0));
        }
    }
}
