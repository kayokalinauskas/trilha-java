package dev.trilhajava.m10.aulas.junit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Não testa a agenda: mostra, no console, em que ordem o JUnit chama cada método. */
class CicloDeVidaTest {

    private static int instanciasCriadas = 0;

    private final int numeroDaInstancia;
    private int consultasMarcadas = 0;   // campo comum: cada instância tem o seu

    CicloDeVidaTest() {
        numeroDaInstancia = ++instanciasCriadas;
        System.out.println("  construtor: instância #" + numeroDaInstancia);
    }

    @BeforeAll
    static void antesDeTodos() {
        System.out.println("@BeforeAll: uma vez, antes de qualquer instância");
    }

    @BeforeEach
    void antesDeCada() {
        System.out.println("    @BeforeEach na instância #" + numeroDaInstancia);
    }

    @Test
    void primeiroTeste() {
        consultasMarcadas++;
        System.out.println("      @Test primeiroTeste: consultasMarcadas = " + consultasMarcadas);
        assertThat(consultasMarcadas).isEqualTo(1);
    }

    @Test
    void segundoTeste() {
        consultasMarcadas++;
        System.out.println("      @Test segundoTeste: consultasMarcadas = " + consultasMarcadas);
        assertThat(consultasMarcadas).isEqualTo(1);   // não é 2: a instância é nova
    }

    @AfterEach
    void depoisDeCada() {
        System.out.println("    @AfterEach na instância #" + numeroDaInstancia);
    }

    @AfterAll
    static void depoisDeTodos() {
        System.out.println("@AfterAll: uma vez, no fim (" + instanciasCriadas + " instâncias criadas)");
    }
}
