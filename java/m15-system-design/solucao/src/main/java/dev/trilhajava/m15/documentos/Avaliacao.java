package dev.trilhajava.m15.documentos;

/** Embutida no produto: sempre lida junto com ele, nunca sozinha. */
public record Avaliacao(String autor, int nota, String comentario) {

    public Avaliacao {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("nota deve ser de 1 a 5");
        }
    }
}
