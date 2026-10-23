package dev.trilhajava.m11.aulas.senhas;

/**
 * A conta de um paciente no portal da clínica. Guarda o e-mail (já normalizado) e o hash da
 * senha, nunca a senha. O hash carrega o prefixo do algoritmo, como {@code {argon2id}$argon2id$...}.
 */
public record Conta(String email, String hashDaSenha) {

    public Conta comHash(String novoHash) {
        return new Conta(email, novoHash);
    }
}
