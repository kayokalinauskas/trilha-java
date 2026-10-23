package dev.trilhajava.m10.aulas.arquitetura;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import com.tngtech.archunit.library.freeze.ViolationStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * O que o FreezingArchRule faz, passo a passo: grava as violações de hoje e, na próxima execução,
 * só acusa as NOVAS. Usa um store em memória para o teste não depender de arquivos.
 */
class CongelamentoTest {

    static final JavaClasses LEGADO = new ClassFileImporter()
            .importPackages("dev.trilhajava.m10.aulas.arquitetura.legado");

    static final JavaClasses LEGADO_MAIS_PULL_REQUEST = new ClassFileImporter()
            .importPackages(
                    "dev.trilhajava.m10.aulas.arquitetura.legado",
                    "dev.trilhajava.m10.aulas.arquitetura.pullrequest");

    @Test
    void aceitaAsViolacoesAntigasEAcusaANova() {
        var store = new StoreEmMemoria();
        ArchRule camadasCongeladas = FreezingArchRule.freeze(ArquiteturaDoConsultorioTest.CAMADAS).persistIn(store);

        // 1ª execução: as 3 violações do legado são gravadas, e a regra passa.
        assertThatCode(() -> camadasCongeladas.check(LEGADO)).doesNotThrowAnyException();

        // 2ª execução, com o pull request: só as violações novas (as 3 do RelatorioController) são acusadas.
        assertThatThrownBy(() -> camadasCongeladas.check(LEGADO_MAIS_PULL_REQUEST))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("was violated (3 times)")
                .hasMessageContaining("RelatorioController.totalDeConsultas()> calls method <")
                .hasMessageNotContaining("ConsultaController");
    }

    /** Um ViolationStore mínimo: guarda, por regra, a lista de violações. */
    static class StoreEmMemoria implements ViolationStore {

        private final Map<String, List<String>> violacoes = new HashMap<>();

        @Override
        public void initialize(Properties propriedades) {
        }

        @Override
        public boolean contains(ArchRule regra) {
            return violacoes.containsKey(regra.getDescription());
        }

        @Override
        public void save(ArchRule regra, List<String> novas) {
            violacoes.put(regra.getDescription(), List.copyOf(novas));
        }

        @Override
        public List<String> getViolations(ArchRule regra) {
            return violacoes.getOrDefault(regra.getDescription(), List.of());
        }
    }
}
