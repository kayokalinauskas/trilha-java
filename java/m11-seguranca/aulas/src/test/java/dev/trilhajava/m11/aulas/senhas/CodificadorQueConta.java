package dev.trilhajava.m11.aulas.senhas;

import org.springframework.security.crypto.password.PasswordEncoder;

/** Delega para o codificador real e conta as conferências: é o "cronômetro" dos testes. */
class CodificadorQueConta implements PasswordEncoder {

    private final PasswordEncoder real;
    int conferencias;

    CodificadorQueConta(PasswordEncoder real) {
        this.real = real;
    }

    @Override
    public String encode(CharSequence senha) {
        return real.encode(senha);
    }

    @Override
    public boolean matches(CharSequence senha, String hash) {
        conferencias++;
        return real.matches(senha, hash);
    }

    @Override
    public boolean upgradeEncoding(String hash) {
        return real.upgradeEncoding(hash);
    }
}
