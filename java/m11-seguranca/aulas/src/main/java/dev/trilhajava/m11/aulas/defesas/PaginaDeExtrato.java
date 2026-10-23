package dev.trilhajava.m11.aulas.defesas;

import java.util.List;

public record PaginaDeExtrato(int pagina, int tamanho, int totalDeLancamentos, List<Lancamento> lancamentos) {
}
