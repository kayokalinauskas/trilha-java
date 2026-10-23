package dev.trilhajava.m13.aulas.padroes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Template Method sem herança: o esqueleto da leitura de um arquivo de retorno (conferir o
 * cabeçalho, pular linhas em branco, separar colunas, juntar os erros com o número da
 * linha) fica aqui; o único passo que varia, interpretar as colunas, chega como função.
 */
public final class LeitorDeRetorno<T> {

    private final String cabecalhoEsperado;
    private final int colunas;
    private final Function<String[], T> interpretar;

    public LeitorDeRetorno(String cabecalhoEsperado, int colunas, Function<String[], T> interpretar) {
        this.cabecalhoEsperado = cabecalhoEsperado;
        this.colunas = colunas;
        this.interpretar = interpretar;
    }

    public Leitura<T> ler(List<String> linhas) {
        if (linhas.isEmpty() || !linhas.getFirst().equals(cabecalhoEsperado)) {
            throw new IllegalArgumentException("cabeçalho inesperado; esperava: " + cabecalhoEsperado);
        }
        List<T> registros = new ArrayList<>();
        List<String> erros = new ArrayList<>();
        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) {
                continue;
            }
            String[] partes = linha.split(";", -1);
            if (partes.length != colunas) {
                erros.add("linha " + (i + 1) + ": esperava " + colunas + " colunas, veio " + partes.length);
                continue;
            }
            try {
                registros.add(interpretar.apply(partes));
            } catch (RuntimeException e) {
                erros.add("linha " + (i + 1) + ": " + e.getMessage());
            }
        }
        return new Leitura<>(List.copyOf(registros), List.copyOf(erros));
    }

    public record Leitura<T>(List<T> registros, List<String> erros) {
    }
}
