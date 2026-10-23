package dev.trilhajava.m08.pacientes;

import java.util.List;

/** Envelope de paginação: o cliente sabe onde está e quantas páginas existem. */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {

    static <T> Pagina<T> de(List<T> todos, int pagina, int tamanho) {
        int inicio = (int) Math.min((long) pagina * tamanho, todos.size());
        int fim = Math.min(inicio + tamanho, todos.size());
        int totalPaginas = (int) Math.ceil((double) todos.size() / tamanho);
        return new Pagina<>(List.copyOf(todos.subList(inicio, fim)), pagina, tamanho, todos.size(), totalPaginas);
    }
}
