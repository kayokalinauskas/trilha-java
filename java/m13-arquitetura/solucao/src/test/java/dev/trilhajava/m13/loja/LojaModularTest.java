package dev.trilhajava.m13.loja;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import dev.trilhajava.m13.loja.estoque.Estoque;
import dev.trilhajava.m13.loja.faturamento.AutorizadorFiscal;
import dev.trilhajava.m13.loja.faturamento.Faturas;
import dev.trilhajava.m13.loja.pedidos.Item;
import dev.trilhajava.m13.loja.pedidos.PedidoConfirmado;
import dev.trilhajava.m13.loja.pedidos.ServicoDePedidos;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@DisplayName("Exercício 13.03 — monólito modular com Spring Modulith")
class LojaModularTest {

    static void esperarAte(BooleanSupplier condicao, String descricao) throws InterruptedException {
        long limite = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (!condicao.getAsBoolean()) {
            if (System.nanoTime() > limite) {
                throw new AssertionError("não aconteceu em 10 s: " + descricao);
            }
            Thread.sleep(20);
        }
    }

    @Nested
    @DisplayName("a estrutura dos módulos")
    class Estrutura {

        final JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.trilhajava.m13.loja");

        @Test
        @DisplayName("os módulos são válidos: sem ciclos e sem ninguém usando os pacotes internos dos outros")
        void verify() {
            // detectViolations() sempre executa; verify() roda uma vez por instância (e ApplicationModules.of
            // guarda as instâncias em cache: um @ApplicationModuleTest anterior já teria "gastado" a verificação).
            ApplicationModules.of(LojaApplication.class).detectViolations().throwIfPresent();
        }

        @Test
        @DisplayName("pedidos não depende de estoque nem de faturamento")
        void pedidosIndependente() {
            noClasses().that().resideInAPackage("..loja.pedidos..")
                    .should().dependOnClassesThat().resideInAnyPackage("..loja.estoque..", "..loja.faturamento..")
                    .check(classes);
        }

        @Test
        @DisplayName("estoque e faturamento reagem ao PedidoConfirmado, e é só isso que conhecem de pedidos")
        void conhecemSoOEvento() {
            for (String modulo : List.of("estoque", "faturamento")) {
                assertThat(classes.stream()
                        .filter(c -> c.getPackageName().startsWith("dev.trilhajava.m13.loja." + modulo))
                        .anyMatch(c -> c.getDirectDependenciesFromSelf().stream()
                                .anyMatch(d -> d.getTargetClass().isEquivalentTo(PedidoConfirmado.class))))
                        .as("alguma classe de %s usa o PedidoConfirmado", modulo).isTrue();
            }
            DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> apiDoEvento =
                    type(PedidoConfirmado.class).or(type(Item.class));
            noClasses().that().resideInAnyPackage("..loja.estoque..", "..loja.faturamento..")
                    .should().dependOnClassesThat(resideInAPackage("..loja.pedidos..").and(DescribedPredicate.not(apiDoEvento)))
                    .check(classes);
        }
    }

    @Nested
    @ApplicationModuleTest(module = "estoque", classes = LojaApplication.class)
    @DisplayName("o módulo estoque, sozinho")
    class ModuloDeEstoque {

        @Autowired
        Estoque estoque;

        @Test
        @DisplayName("um PedidoConfirmado publicado baixa o estoque (sem o módulo pedidos no contexto)")
        void baixa(Scenario cenario) {
            int antes = estoque.disponivel("CANECA");
            var evento = new PedidoConfirmado(UUID.randomUUID(), "ana",
                    List.of(new Item("CANECA", 3, new BigDecimal("39.90"))), new BigDecimal("119.70"));

            cenario.publish(evento)
                    .andWaitForStateChange(() -> estoque.disponivel("CANECA"), saldo -> saldo == antes - 3)
                    .andVerify(saldo -> assertThat(saldo).isEqualTo(antes - 3));
        }
    }

    @Nested
    @SpringBootTest(classes = LojaApplication.class)
    @RecordApplicationEvents
    @DisplayName("a aplicação inteira")
    class Integracao {

        @Autowired
        ServicoDePedidos pedidos;

        @Autowired
        Estoque estoque;

        @Autowired
        Faturas faturas;

        @Autowired
        IncompleteEventPublications incompletas;

        @Autowired
        ApplicationEvents eventos;

        @MockitoBean
        AutorizadorFiscal sefaz;

        UUID pedidoDeCamisetas() {
            return pedidos.criar("ana", List.of(
                    new Item("CAMISETA-P", 2, new BigDecimal("59.90")),
                    new Item("CANECA", 1, new BigDecimal("39.90"))));
        }

        @Test
        @DisplayName("confirmar publica PedidoConfirmado; estoque e faturamento reagem depois")
        void fluxoCompleto() throws Exception {
            given(sefaz.autorizar(any(), any())).willReturn("35260912345678000199550010000012341000012345");
            int antes = estoque.disponivel("CAMISETA-P");
            UUID id = pedidoDeCamisetas();

            pedidos.confirmar(id);

            assertThat(eventos.stream(PedidoConfirmado.class))
                    .singleElement()
                    .satisfies(e -> {
                        assertThat(e.pedido()).isEqualTo(id);
                        assertThat(e.total()).isEqualByComparingTo("159.70");
                    });
            esperarAte(() -> estoque.disponivel("CAMISETA-P") == antes - 2, "a baixa do estoque");
            esperarAte(() -> faturas.doPedido(id).isPresent(), "a fatura");
            assertThat(faturas.doPedido(id).orElseThrow().total()).isEqualByComparingTo("159.70");
        }

        @Test
        @DisplayName("a SEFAZ fora do ar não impede a confirmação, e o estoque é baixado mesmo assim")
        void sefazForaDoAr() throws Exception {
            given(sefaz.autorizar(any(), any())).willThrow(new IllegalStateException("SEFAZ indisponível"));
            int antes = estoque.disponivel("CAMISETA-P");
            UUID id = pedidoDeCamisetas();

            pedidos.confirmar(id);

            assertThat(pedidos.confirmado(id)).isTrue();
            esperarAte(() -> estoque.disponivel("CAMISETA-P") == antes - 2, "a baixa do estoque");
            verify(sefaz, timeout(5_000)).autorizar(any(), any());
            assertThat(faturas.doPedido(id)).isEmpty();
        }

        @Test
        @DisplayName("a publicação que falhou fica incompleta e é reenviada: a fatura sai quando a SEFAZ volta (e o estoque não baixa duas vezes)")
        void reenvio() throws Exception {
            given(sefaz.autorizar(any(), any()))
                    .willThrow(new IllegalStateException("SEFAZ indisponível"))
                    .willReturn("35260912345678000199550010000012341000099999");
            int antes = estoque.disponivel("CAMISETA-P");
            UUID id = pedidoDeCamisetas();
            pedidos.confirmar(id);
            verify(sefaz, timeout(5_000)).autorizar(any(), any());
            esperarAte(() -> estoque.disponivel("CAMISETA-P") == antes - 2, "a baixa do estoque");
            Thread.sleep(300);   // a transação do listener que falhou termina
            assertThat(faturas.doPedido(id)).isEmpty();

            incompletas.resubmitIncompletePublications(publicacao -> true);

            esperarAte(() -> faturas.doPedido(id).isPresent(), "a fatura, depois do reenvio");
            verify(sefaz, atLeastOnce()).autorizar(any(), any());
            assertThat(faturas.doPedido(id).orElseThrow().chaveDeAcesso()).endsWith("99999");
            Thread.sleep(300);
            assertThat(estoque.disponivel("CAMISETA-P")).isEqualTo(antes - 2);
        }
    }
}
