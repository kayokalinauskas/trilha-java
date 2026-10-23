package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.aplicacao.Pagina;
import java.util.List;

public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {

    static <T> PaginaResponse<T> de(Pagina<T> pagina) {
        return new PaginaResponse<>(pagina.itens(), pagina.pagina(), pagina.tamanho(), pagina.totalItens(),
                pagina.totalPaginas());
    }
}
