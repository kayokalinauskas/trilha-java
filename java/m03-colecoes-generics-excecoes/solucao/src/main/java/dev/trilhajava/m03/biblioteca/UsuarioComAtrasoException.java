package dev.trilhajava.m03.biblioteca;

public class UsuarioComAtrasoException extends BibliotecaException {

    public UsuarioComAtrasoException(String usuarioId) {
        super("USUARIO_COM_ATRASO", "Usuário " + usuarioId + " tem empréstimo em atraso e não pode pegar novos livros");
    }
}
