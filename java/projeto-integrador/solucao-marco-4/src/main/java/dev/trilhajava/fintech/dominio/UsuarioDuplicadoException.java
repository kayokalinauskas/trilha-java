package dev.trilhajava.fintech.dominio;

public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException() {
        super("Já existe um usuário com este e-mail");
    }
}
