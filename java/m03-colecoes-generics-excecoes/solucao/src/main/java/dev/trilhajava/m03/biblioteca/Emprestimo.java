package dev.trilhajava.m03.biblioteca;

import java.time.LocalDate;

public record Emprestimo(long id, String isbn, String usuarioId, LocalDate dataEmprestimo, LocalDate dataPrevista) {

    public boolean atrasadoEm(LocalDate data) {
        return data.isAfter(dataPrevista);
    }
}
