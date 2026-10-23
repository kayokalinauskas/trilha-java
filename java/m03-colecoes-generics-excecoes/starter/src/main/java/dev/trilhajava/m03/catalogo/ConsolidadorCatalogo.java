package dev.trilhajava.m03.catalogo;

import java.util.Collection;

/**
 * Exercício 03.01 — junta duas fontes em um catálogo sem duplicados.
 * Rode: mvn test -Dtest=ConsolidadorCatalogoTest
 *
 * - registros sem código, sem nome, sem preço ou sem data são rejeitados (e contados);
 * - entre duplicados, vence o de data mais recente; em empate, vence o sistema novo;
 * - a lista final vem ordenada por código.
 */
public class ConsolidadorCatalogo {

    public ResultadoConsolidacao consolidar(Collection<RegistroPeca> legado, Collection<RegistroPeca> novo) {
        throw new UnsupportedOperationException("TODO");
    }
}
