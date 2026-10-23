package dev.trilhajava.m10.aulas.desempenho;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * A mesma conta de distância medida de cinco jeitos: uma régua (o método vazio),
 * duas armadilhas e duas formas corretas.
 *
 * <p>Rode com: {@code mvn test-compile exec:exec@jmh -Djmh.filtro=ArmadilhasDoJit}
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class ArmadilhasDoJitBenchmark {

    // Centro de distribuição e endereço do cliente numa grade da cidade, em km.
    static final double X_CD = 2.5;
    static final double Y_CD = 1.0;
    static final double X_CLIENTE = 14.2;
    static final double Y_CLIENTE = 9.7;

    // Os mesmos valores em campos comuns: para o JIT, são valores que podem mudar.
    double xCd = X_CD;
    double yCd = Y_CD;
    double xCliente = X_CLIENTE;
    double yCliente = Y_CLIENTE;

    /** Referência: um método vazio. É o "zero" da régua. */
    @Benchmark
    public void metodoVazio() {
    }

    /** ERRADO: o resultado é descartado, e o JIT pode apagar a conta inteira (dead code elimination). */
    @Benchmark
    public void resultadoIgnorado() {
        Distancia.kmNaGrade(xCd, yCd, xCliente, yCliente);
    }

    /** ERRADO: com constantes, o JIT pode calcular o resultado uma vez só (constant folding). */
    @Benchmark
    public double comConstantes() {
        return Distancia.kmNaGrade(X_CD, Y_CD, X_CLIENTE, Y_CLIENTE);
    }

    /** CERTO: devolver o valor faz o JMH consumi-lo, e o JIT não pode apagar a conta. */
    @Benchmark
    public double resultadoDevolvido() {
        return Distancia.kmNaGrade(xCd, yCd, xCliente, yCliente);
    }

    /** CERTO: com mais de um resultado, entregue cada um ao Blackhole. */
    @Benchmark
    public void comBlackhole(Blackhole buraco) {
        buraco.consume(Distancia.kmNaGrade(xCd, yCd, xCliente, yCliente));
    }
}
