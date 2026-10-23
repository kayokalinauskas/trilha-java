package dev.trilhajava.m10.aulas.desempenho;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Antes de calcular o frete, o checkout confere se o CEP está numa área com restrição de entrega.
 * Os CEPs restritos ficam em memória. Uma List ou um HashSet? Depende do tamanho, e é isso que
 * este benchmark mede, para 10, 1.000 e 100.000 CEPs restritos.
 *
 * <p>Rode com: {@code mvn test-compile exec:exec@jmh -Djmh.filtro=RestricaoDeEntrega}
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class RestricaoDeEntregaBenchmark {

    @Param({"10", "1000", "100000"})
    int quantidadeDeCeps;

    List<String> cepsNaLista;
    Set<String> cepsNoConjunto;

    /** Um CEP que NÃO está restrito: o caso mais comum, e o pior para a lista (percorre tudo). */
    String cepConsultado;

    @Setup
    public void prepararCeps() {
        cepsNaLista = new ArrayList<>(quantidadeDeCeps);
        for (int i = 0; i < quantidadeDeCeps; i++) {
            cepsNaLista.add("%08d".formatted(20_000_000 + i * 7));
        }
        cepsNoConjunto = new HashSet<>(cepsNaLista);
        cepConsultado = "01310100";
    }

    @Benchmark
    public boolean comLista() {
        return cepsNaLista.contains(cepConsultado);
    }

    @Benchmark
    public boolean comHashSet() {
        return cepsNoConjunto.contains(cepConsultado);
    }
}
